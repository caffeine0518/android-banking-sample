package com.study.bank.feature.home.ui.money

import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import java.text.DecimalFormat

fun Money.format(): String {
    val pattern = if (currency.exponent == 0) "#,##0" else "#,##0." + "0".repeat(currency.exponent)
    val number = DecimalFormat(pattern).format(amount)
    return when (currency) {
        Currency.KRW -> "${number}원"
        Currency.USD -> "\$$number"
        Currency.EUR -> "€$number"
        Currency.JPY -> "￥$number"
    }
}
