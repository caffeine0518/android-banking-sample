package com.study.bank.feature.account.ui.model

import com.study.bank.core.ui.model.MoneyUi

data class AccountUi(
    val id: String,
    val bankDisplayName: String,
    val type: AccountTypeUi,
    val nickname: String?,
    val numberMasked: String,
    val balance: MoneyUi,
)
