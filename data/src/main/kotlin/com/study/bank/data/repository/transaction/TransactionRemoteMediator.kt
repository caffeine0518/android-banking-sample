package com.study.bank.data.repository.transaction

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.local.entity.TransactionEntity
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.domain.coroutine.cancellableCatching
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.account.AccountId

/**
 * 거래내역 페이징의 네트워크→DB 적재기(가이드의 network+DB 패턴). UI/Pager는 Room만 보고, 이 미디에이터가
 * KFTC 연속조회로 페이지를 받아 [transactionDao]에 쓴다. 쓰면 Room PagingSource가 무효화→자동 재로드된다.
 *
 * KFTC 연속조회 커서(befor_inquiry_trace_info)는 [nextCursor] 메모리 필드로 보관한다 — 미디에이터 인스턴스가
 * 한 Pager 스트림의 수명을 함께하므로 REFRESH→APPEND 사이에 유지되고, 재생성되면 REFRESH로 리셋된다.
 * (DB가 인메모리라 프로세스 사망 시 캐시·커서가 함께 사라지므로, 가이드의 RemoteKeys 테이블 같은 영속화는 이득이 없다.)
 *
 * REFRESH의 캐시 교체는 [TransactionDao.replaceForAccount](@Transaction)로 원자적이고, APPEND는 단순 insert다.
 * 네트워크 실패는 [MediatorResult.Error]로, 코루틴 취소는 [cancellableCatching]으로 그대로 전파한다.
 * 전방 전용이라 PREPEND는 즉시 종료한다.
 *
 * 주의(UI 배선 제약): 이 페이징 경로와 레거시 [com.study.bank.domain.repository.TransactionRepository.refresh]·
 * observeTransactions(Room 직접 교체)는 **같은 transactions 테이블을 공유하므로 동시에 활성화하면 안 된다**.
 * refresh가 테이블을 비워도 [nextCursor]는 그대로라 다음 APPEND가 먼 페이지를 받아 중간이 누락된다. AccountDetail을
 * 페이징으로 옮길 때 거래 레거시 경로를 걷어내고 pagingItems.refresh()(=Pager가 RemoteMediator REFRESH 재실행)로 일원화한다.
 */
@OptIn(ExperimentalPagingApi::class)
internal class TransactionRemoteMediator(
    private val accountId: AccountId,
    private val api: KftcApiService,
    private val dtoMapper: TransactionMapper,
    private val entityMapper: TransactionEntityMapper,
    private val transactionDao: TransactionDao,
    private val bankTranId: String,
    private val fromDate: String,
    private val toDate: String,
    private val tranDtime: String,
) : RemoteMediator<Int, TransactionEntity>() {

    // KFTC 연속조회 커서. null이면 더 받을 페이지가 없다. load는 직렬화되지만 디스패처 스레드가 달라질 수 있어 @Volatile.
    @Volatile
    private var nextCursor: String? = null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>,
    ): MediatorResult = when (loadType) {
        // 전방 전용 — 위쪽(더 최신)으로 받을 건 없다. 현재 특정 구간 선진입 x
        LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
        // 첫 페이지부터 다시 받아 계좌 캐시를 통째 교체한다.
        LoadType.REFRESH -> loadPage(cursor = null, replaceCache = true)
        // 저장된 커서로 다음 페이지를 이어 받는다. 커서가 없으면 더 받을 게 없다.
        LoadType.APPEND -> {
            if (nextCursor == null) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                loadPage(cursor = nextCursor, replaceCache = false)
            }
        }
    }

    /** 한 페이지를 받아 [transactionDao]에 적재하고 다음 커서를 갱신한다. REFRESH는 캐시 교체, APPEND는 추가. */
    private suspend fun loadPage(cursor: String?, replaceCache: Boolean): MediatorResult =
        cancellableCatching {
            val response = fetchPage(cursor)
            val currency = resolveCurrency(response)
            val entities = toEntities(response, currency)

            if (replaceCache) {
                transactionDao.replaceForAccount(accountId.value, entities)
            } else {
                transactionDao.insertAll(entities)
            }
            nextCursor = response.beforInquiryTraceInfo
                .takeIf { response.nextPageYn == NEXT_PAGE_YES }

            MediatorResult.Success(endOfPaginationReached = nextCursor == null)
        }.getOrElse {
            MediatorResult.Error(it)
        }

    private fun toEntities(
        response: TransactionListResponse,
        currency: Currency
    ): List<TransactionEntity> = response.resList.map { item ->
        entityMapper.toEntity(dtoMapper.map(item, accountId, currency))
    }

    private fun resolveCurrency(response: TransactionListResponse): Currency =
        Currency.requireByCode(response.currencyCode)

    private suspend fun fetchPage(cursor: String?): TransactionListResponse = api.getTransactionList(
        bankTranId = bankTranId,
        fintechUseNum = accountId.value,
        fromDate = fromDate,
        toDate = toDate,
        tranDtime = tranDtime,
        beforInquiryTraceInfo = cursor,
    )

    private companion object {
        const val NEXT_PAGE_YES = "Y"
    }
}
