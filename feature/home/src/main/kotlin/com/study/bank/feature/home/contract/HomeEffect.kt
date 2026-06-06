package com.study.bank.feature.home.contract

sealed interface HomeEffect {
    data class NavigateToAccountDetail(val accountId: String) : HomeEffect
}
