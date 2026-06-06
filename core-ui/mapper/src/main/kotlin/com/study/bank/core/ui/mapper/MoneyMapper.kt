package com.study.bank.core.ui.mapper

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money

fun Money.toUi(): MoneyUi = MoneyUi(
    amount = amount,
    currency = currency.toUi(),
)

fun Currency.toUi(): CurrencyUi = when (this) {
    Currency.KRW -> CurrencyUi.KRW
    Currency.USD -> CurrencyUi.USD
    Currency.JPY -> CurrencyUi.JPY
    Currency.EUR -> CurrencyUi.EUR
}
