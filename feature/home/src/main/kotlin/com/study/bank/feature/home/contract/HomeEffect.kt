package com.study.bank.feature.home.contract

import com.study.bank.domain.model.account.AccountId

sealed interface HomeEffect {
    data class NavigateToAccountDetail(val accountId: AccountId) : HomeEffect
}
