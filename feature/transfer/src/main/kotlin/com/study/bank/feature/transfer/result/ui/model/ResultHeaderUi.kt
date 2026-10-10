package com.study.bank.feature.transfer.result.ui.model

import com.study.bank.core.ui.model.MoneyUi

data class ResultHeaderUi(
    val recipientName: String,
    val amount: MoneyUi,
)
