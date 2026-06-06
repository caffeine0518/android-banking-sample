package com.study.bank.feature.home.contract

import com.study.bank.domain.model.account.Account

data class HomeState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
)
