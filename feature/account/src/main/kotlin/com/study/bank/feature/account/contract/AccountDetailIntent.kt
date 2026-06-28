package com.study.bank.feature.account.contract

import com.study.bank.domain.model.account.Account

sealed interface AccountDetailAction

sealed interface AccountDetailIntent : AccountDetailAction {
    data object Refresh : AccountDetailIntent
    data object SendClicked : AccountDetailIntent
    data object BackClicked : AccountDetailIntent
}

internal sealed interface AccountDetailInternalAction : AccountDetailAction {
    data class AccountUpdated(val account: Account?) : AccountDetailInternalAction
    data class RefreshFinished(val error: Throwable? = null) : AccountDetailInternalAction
}
