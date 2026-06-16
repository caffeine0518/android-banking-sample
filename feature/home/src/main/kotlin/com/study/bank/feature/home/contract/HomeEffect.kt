package com.study.bank.feature.home.contract

sealed interface HomeEffect {
    data class NavigateToAccountDetail(val accountId: String) : HomeEffect

    /** refresh 실패를 사용자에게 일회성으로 알림(스낵바 등). */
    data object ShowRefreshError : HomeEffect
}
