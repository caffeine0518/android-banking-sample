package com.study.bank.feature.account.ui.model

import com.study.bank.core.ui.model.MoneyUi

data class TransactionUi(
    val id: String,
    val type: TransactionTypeUi,
    val counterpartyName: String?,
    val amount: MoneyUi,
    val occurredAtLabel: String,
)
