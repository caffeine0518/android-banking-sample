package com.study.bank.feature.transfer.amount.ui.model

import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi

data class AmountSourceUi(
    val nickname: String?,
    val type: AccountTypeUi,
    val balance: MoneyUi,
)
