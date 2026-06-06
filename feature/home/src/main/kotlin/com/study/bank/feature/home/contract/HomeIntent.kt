package com.study.bank.feature.home.contract

sealed interface HomeIntent {
    data object Load : HomeIntent
    data object Refresh : HomeIntent
    data class AccountClicked(val accountId: String) : HomeIntent
}
