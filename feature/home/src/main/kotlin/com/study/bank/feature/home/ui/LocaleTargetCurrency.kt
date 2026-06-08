package com.study.bank.feature.home.ui

import com.study.bank.domain.model.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** 사용자 시스템 로케일에서 표시 통화 해석. 실패 시 [Currency.DEFAULT]로 폴백. */
@Singleton
class LocaleTargetCurrency @Inject constructor() {

    fun resolve(): Currency = Currency.byCodeOrDefault(localeCurrencyCode())

    private fun localeCurrencyCode(): String? =
        runCatching { java.util.Currency.getInstance(Locale.getDefault()).currencyCode }.getOrNull()
}
