package com.study.bank.core.ui.mapper

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoneyUiMapper @Inject constructor() {

    fun map(money: Money): MoneyUi = MoneyUi(
        amount = money.amount,
        currency = mapCurrency(money.currency),
    )

    fun mapCurrency(currency: Currency): CurrencyUi = when (currency) {
        Currency.KRW -> CurrencyUi.KRW
        Currency.USD -> CurrencyUi.USD
        Currency.JPY -> CurrencyUi.JPY
        Currency.EUR -> CurrencyUi.EUR
    }
}
