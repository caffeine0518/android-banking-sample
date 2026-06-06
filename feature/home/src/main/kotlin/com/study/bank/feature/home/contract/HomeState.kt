package com.study.bank.feature.home.contract

import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.home.ui.model.AccountUi

data class HomeState(
    val accounts: List<AccountUi> = emptyList(),
    val totalsByCurrency: List<MoneyUi> = emptyList(),
    val isLoading: Boolean = false,
)
