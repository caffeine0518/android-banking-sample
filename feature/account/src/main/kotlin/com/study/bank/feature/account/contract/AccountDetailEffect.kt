package com.study.bank.feature.account.contract

sealed interface AccountDetailEffect {

    data class NavigateToTransfer(val accountId: String) : AccountDetailEffect

    data object NavigateBack : AccountDetailEffect

    data object ShowRefreshError : AccountDetailEffect
}
