package com.study.bank.feature.account.contract

import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.transaction.Transaction

sealed interface AccountDetailAction

sealed interface AccountDetailIntent : AccountDetailAction {
    data object Refresh : AccountDetailIntent
    data object SendClicked : AccountDetailIntent
    data object BackClicked : AccountDetailIntent
}

internal sealed interface AccountDetailInternalAction : AccountDetailAction {
    data class AccountUpdated(val account: Account?) : AccountDetailInternalAction
    data class TransactionsUpdated(val transactions: List<Transaction>) : AccountDetailInternalAction
    data class RefreshFinished(val error: Throwable? = null) : AccountDetailInternalAction
}
