package com.study.bank.data.repository.transaction

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 거래내역 SSOT = Room. 단건 목록 읽기([observeTransactions])는 [dao] Flow만 본다.
 *
 * 원격(KFTC)은 [refresh]에서만 호출돼 결과를 해당 계좌 캐시에 통째 교체 기록한다. 따라서 화면은 항상
 * 로컬 캐시를 관찰하고 갱신은 별도 트리거로 일어난다([com.study.bank.data.repository.account.AccountRepositoryImpl] 패턴).
 *
 * [transactionStream]도 SSOT는 Room이다(가이드의 network+DB 패턴) — UI/Pager는 [TransactionDao.pagingSource]만
 * 관찰하고, [TransactionRemoteMediator]가 KFTC 연속조회로 페이지를 받아 Room에 적재한다. 월급통장처럼 수천 건인
 * 계좌도 화면에 보이는 만큼만 점진 적재된다.
 */
@OptIn(ExperimentalPagingApi::class)
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val dao: TransactionDao,
    private val dtoMapper: TransactionMapper,
    private val entityMapper: TransactionEntityMapper,
) : TransactionRepository {

    override fun observeTransactions(accountId: AccountId): Flow<List<Transaction>> =
        dao.observeByAccountId(accountId.value)
            .map { entities -> entities.map(entityMapper::toDomain) }
            .distinctUntilChanged()

    override suspend fun refresh(accountId: AccountId) {
        val transactions = fetchFromKftc(accountId)
        dao.replaceForAccount(accountId.value, transactions.map(entityMapper::toEntity))
    }

    override fun transactionStream(accountId: AccountId): Flow<PagingData<Transaction>> =
        Pager(
            // initialLoadSize 기본값은 pageSize×3. 서버 페이지가 KFTC_TRANSACTION_PAGE_SIZE 고정이라 그대로면 첫 화면에
            // REFRESH+APPEND+APPEND(=3 왕복)가 직렬로 일어난다. 서버 페이지 크기에 맞춰 첫 화면을 1왕복으로 채운다.
            config = PagingConfig(
                pageSize = KFTC_TRANSACTION_PAGE_SIZE,
                initialLoadSize = KFTC_TRANSACTION_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = TransactionRemoteMediator(
                accountId = accountId,
                api = api,
                dtoMapper = dtoMapper,
                entityMapper = entityMapper,
                transactionDao = dao,
                bankTranId = bankTranIdFor(accountId.value),
                fromDate = FROM_DATE,
                toDate = TO_DATE,
                tranDtime = TRAN_DTIME,
            ),
            pagingSourceFactory = { dao.pagingSource(accountId.value) },
        ).flow.map { pagingData -> pagingData.map(entityMapper::toDomain) }

    private suspend fun fetchFromKftc(accountId: AccountId): List<Transaction> {
        val response = api.getTransactionList(
            bankTranId = bankTranIdFor(accountId.value),
            fintechUseNum = accountId.value,
            fromDate = FROM_DATE,
            toDate = TO_DATE,
            tranDtime = TRAN_DTIME,
        )
        val currency = Currency.requireByCode(response.currencyCode)
        return response.resList.map { item ->
            dtoMapper.map(item, accountId, currency)
        }
    }

    private companion object {
        // 데모 고정값. 실서비스는 조회 기간/요청 추적자(bank_tran_id, tran_dtime)를 동적으로 구성한다.
        const val FROM_DATE = "20260101"
        const val TO_DATE = "20261231"
        const val TRAN_DTIME = "20260603120000"

        fun bankTranIdFor(fintechUseNum: String): String =
            "M202300001U%06d".format(fintechUseNum.hashCode() and 0xFFFFF)
    }
}
