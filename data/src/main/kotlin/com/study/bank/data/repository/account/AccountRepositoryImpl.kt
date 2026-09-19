package com.study.bank.data.repository.account

import android.util.Log
import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.repository.TRAN_DTIME
import com.study.bank.data.repository.bankTranIdFor
import com.study.bank.domain.coroutine.cancellableCatching
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val dao: AccountDao,
    private val dtoMapper: AccountMapper,
    private val entityMapper: AccountEntityMapper,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        dao.observeAll()
            .map { entities -> entities.map(entityMapper::toDomain) }
            .distinctUntilChanged()

    override fun observeAccount(id: AccountId): Flow<Account?> =
        dao.observeById(id.value)
            .map { entity -> entity?.let(entityMapper::toDomain) }
            .distinctUntilChanged()

    override suspend fun findAccount(id: AccountId): Account? =
        dao.findById(id.value)?.let(entityMapper::toDomain)

     override suspend fun refresh() {
        val accounts = fetchFromKftc()
        dao.replaceAll(accounts.map(entityMapper::toEntity))
    }

    private suspend fun fetchFromKftc(): List<Account> {
        val listResponse = api.getAccountList(userSeqNo = USER_SEQ_NO)
        return coroutineScope {
            listResponse.resList.map { item ->
                async {
                    cancellableCatching {
                        val balance = api.getAccountBalance(
                            bankTranId = bankTranIdFor(item.fintechUseNum),
                            fintechUseNum = item.fintechUseNum,
                            tranDtime = TRAN_DTIME,
                        )
                        dtoMapper.map(item, balance)
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to map account ${item.fintechUseNum}, skipping", error)
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
    }

    private companion object {
        const val TAG = "AccountRepository"
        // 데모 전용 고정 사용자. 운영에서는 인증 토큰에서 추출한다.
        const val USER_SEQ_NO = "1100000001"
    }
}
