package com.study.bank.feature.home.ui

import com.study.bank.domain.model.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleTargetCurrency @Inject constructor() {

    fun resolve(): Currency = Currency.byCodeOrDefault(localeCurrencyCode())

    private fun localeCurrencyCode(): String? =
        runCatching { java.util.Currency.getInstance(Locale.getDefault()).currencyCode }.getOrNull()
}
