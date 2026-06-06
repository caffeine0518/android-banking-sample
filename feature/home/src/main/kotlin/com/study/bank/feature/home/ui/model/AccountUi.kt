package com.study.bank.feature.home.ui.model

import com.study.bank.core.ui.model.MoneyUi

data class AccountUi(
    val id: String,
    val bankDisplayName: String,
    val type: AccountTypeUi,
    val nickname: String?,
    val balance: MoneyUi,
)
