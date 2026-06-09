package com.study.bank.domain.repository

import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun observeAccounts(): Flow<List<Account>>

    fun observeAccount(id: AccountId): Flow<Account?>

    suspend fun findAccount(id: AccountId): Account?

    suspend fun refresh()

}
