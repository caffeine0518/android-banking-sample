package com.study.bank.feature.account.contract

import com.study.bank.feature.account.ui.model.AccountUi


data class AccountDetailState(
    val account: AccountUi? = null,
    val isLoading: Boolean = false,
)
