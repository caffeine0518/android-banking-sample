package com.study.bank.feature.home.contract

import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AssetTotals

sealed interface HomeAction

sealed interface HomeIntent : HomeAction {
    data object Refresh : HomeIntent
    data class AccountClicked(val accountId: String) : HomeIntent
}

internal sealed interface HomeInternalAction : HomeAction {
    data class AccountsUpdated(val accounts: List<Account>) : HomeInternalAction
    data class TotalAssetsUpdated(val totals: AssetTotals) : HomeInternalAction
    data class RefreshFinished(val error: Throwable? = null) : HomeInternalAction
}
