package com.study.bank.domain.repository

import com.study.bank.domain.model.Account
import com.study.bank.domain.model.AccountId
import com.study.bank.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccount(id: AccountId): Flow<Account?>
    fun observeTransactions(accountId: AccountId): Flow<List<Transaction>>
}
