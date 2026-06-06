package com.study.bank.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.feature.home.R
import java.text.DecimalFormat

@Composable
fun Money.format(): String {
    val pattern = if (currency.exponent == 0) "#,##0" else "#,##0." + "0".repeat(currency.exponent)
    val number = DecimalFormat(pattern).format(amount)
    val resId = when (currency) {
        Currency.KRW -> R.string.home_money_format_krw
        Currency.USD -> R.string.home_money_format_usd
        Currency.EUR -> R.string.home_money_format_eur
        Currency.JPY -> R.string.home_money_format_jpy
    }
    return stringResource(resId, number)
}
