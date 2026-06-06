package com.study.bank.feature.home.contract

import com.study.bank.domain.model.account.AccountId

sealed interface HomeIntent {
    data object Load : HomeIntent
    data object Refresh : HomeIntent
    data class AccountClicked(val accountId: AccountId) : HomeIntent
}
