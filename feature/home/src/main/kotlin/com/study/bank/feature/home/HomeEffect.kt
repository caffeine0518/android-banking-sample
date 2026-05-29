package com.study.bank.feature.home

import com.study.bank.domain.model.account.AccountId
import com.study.bank.core.ui.mvi.MviEffect

sealed interface HomeEffect : MviEffect {
    data class NavigateToAccountDetail(val accountId: AccountId) : HomeEffect
}
