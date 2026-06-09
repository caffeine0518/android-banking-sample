package com.study.bank.core.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.text.DecimalFormat

@Composable
fun MoneyUi.format(): String {
    val pattern = if (currency.exponent == 0) "#,##0" else "#,##0." + "0".repeat(currency.exponent)
    val number = DecimalFormat(pattern).format(amount)
    val resId = when (currency) {
        CurrencyUi.KRW -> R.string.money_format_krw
        CurrencyUi.USD -> R.string.money_format_usd
        CurrencyUi.EUR -> R.string.money_format_eur
        CurrencyUi.JPY -> R.string.money_format_jpy
        CurrencyUi.TWD -> R.string.money_format_twd
        CurrencyUi.VND -> R.string.money_format_vnd
    }
    return stringResource(resId, number)
}
