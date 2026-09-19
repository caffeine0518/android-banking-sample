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
 * 거래내역 페이징의 네트워크→DB 적재기(network+DB 패턴). [transactionDao]에 쓰면 Room PagingSource가
 * 무효화돼 Pager가 자동 재로드한다. DB가 인메모리라 RemoteKeys 테이블 같은 커서 영속화는 이득이 없다.
 *
 * 주의: [com.study.bank.domain.repository.TransactionRepository.refresh]도 같은 transactions 테이블을
 * 통째 교체하는데 [nextCursor]는 그대로 남는다. 한 계좌에 두 경로가 동시에 살아 있으면 다음 APPEND가 먼
 * 페이지를 받아 중간이 누락된다.
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

    // KFTC 연속조회 커서(befor_inquiry_trace_info). null이면 더 받을 페이지가 없다. 인스턴스가 한 Pager
    // 스트림의 수명을 함께하므로 REFRESH→APPEND 사이에 유지된다. load는 직렬화되지만 디스패처 스레드가
    // 달라질 수 있어 @Volatile.
    @Volatile
    private var nextCursor: String? = null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TransactionEntity>,
    ): MediatorResult = when (loadType) {
        // 전방 전용 — 위쪽(더 최신)으로 받을 건 없다. 특정 구간 선진입도 없다.
        LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
        LoadType.REFRESH -> loadPage(cursor = null, replaceCache = true)
        LoadType.APPEND -> {
            if (nextCursor == null) {
                MediatorResult.Success(endOfPaginationReached = true)
            } else {
                loadPage(cursor = nextCursor, replaceCache = false)
            }
        }
    }

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
