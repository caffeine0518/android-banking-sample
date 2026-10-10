package com.study.bank.feature.transfer.recipient.ui.model

data class AccountUi(
    val id: String,
    val bankDisplayName: String,
    val type: AccountTypeUi,
    val nickname: String?,
    val numberMasked: String,
)
