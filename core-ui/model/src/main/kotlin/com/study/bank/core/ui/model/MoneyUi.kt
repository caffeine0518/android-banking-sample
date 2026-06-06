package com.study.bank.core.ui.model

import java.math.BigDecimal

data class MoneyUi(
    val amount: BigDecimal,
    val currency: CurrencyUi,
)
