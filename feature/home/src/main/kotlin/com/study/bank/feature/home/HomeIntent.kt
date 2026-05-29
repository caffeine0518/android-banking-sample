package com.study.bank.feature.home

import com.study.bank.domain.model.account.AccountId
import com.study.bank.core.ui.mvi.MviIntent

sealed interface HomeIntent : MviIntent {
    data object Refresh : HomeIntent
    data class AccountClicked(val accountId: AccountId) : HomeIntent
}
