package com.study.bank.domain.repository

import androidx.paging.PagingData
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(accountId: AccountId): Flow<List<Transaction>>

    suspend fun refresh(accountId: AccountId)

    fun transactionStream(accountId: AccountId): Flow<PagingData<Transaction>>
}
