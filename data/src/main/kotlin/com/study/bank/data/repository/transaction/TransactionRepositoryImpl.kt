package com.study.bank.data.repository.transaction

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.repository.TRAN_DTIME
import com.study.bank.data.repository.bankTranIdFor
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
 * 거래내역 SSOT = Room. 원격(KFTC)은 [refresh]와 [TransactionRemoteMediator]에서만 호출되고, 화면은 늘
 * 로컬 캐시를 관찰한다([com.study.bank.data.repository.account.AccountRepositoryImpl]와 같은 패턴).
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
        // 데모 고정값. 실서비스는 조회 기간을 동적으로 구성한다.
        const val FROM_DATE = "20260101"
        const val TO_DATE = "20261231"
    }
}
