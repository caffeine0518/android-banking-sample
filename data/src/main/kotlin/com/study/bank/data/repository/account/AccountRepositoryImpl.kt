package com.study.bank.data.repository.account

import android.util.Log
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val api: KftcApiService,
    private val mapper: AccountMapper,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> = flow {
        emit(fetchAccounts())
    }

    override fun observeAccount(id: AccountId): Flow<Account?> =
        observeAccounts().map { accounts -> accounts.firstOrNull { it.id == id } }

    override suspend fun findAccount(id: AccountId): Account? =
        fetchAccounts().firstOrNull { it.id == id }

    private suspend fun fetchAccounts(): List<Account> {
        val listResponse = api.getAccountList(userSeqNo = USER_SEQ_NO)
        return coroutineScope {
            listResponse.resList.map { item ->
                async {
                    runCatching {
                        val balance = api.getAccountBalance(
                            bankTranId = bankTranIdFor(item.fintechUseNum),
                            fintechUseNum = item.fintechUseNum,
                            tranDtime = TRAN_DTIME,
                        )
                        mapper.map(item, balance)
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to map account ${item.fintechUseNum}, skipping", error)
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
    }

    private companion object {
        const val TAG = "AccountRepository"
        // Demo-only fixed user; production extracts from the auth token.
        const val USER_SEQ_NO = "1100000001"
        // tran_dtime is the KFTC envelope tracker; mock skips validation so this is fixed.
        const val TRAN_DTIME = "20260603120000"

        fun bankTranIdFor(fintechUseNum: String): String =
            "M202300001U%06d".format(fintechUseNum.hashCode() and 0xFFFFF)
    }
}
