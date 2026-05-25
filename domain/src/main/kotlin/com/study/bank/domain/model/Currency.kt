package com.study.bank.domain.model

/**
 * Currencies the bank domain explicitly supports.
 *
 * [exponent] is the number of fractional digits in the currency's minor unit
 * (ISO 4217). It governs how [Money] amounts are normalized and rendered.
 */
enum class Currency(val code: String, val exponent: Int) {
    KRW("KRW", 0),
    USD("USD", 2),
    JPY("JPY", 0),
    EUR("EUR", 2),
    ;

    companion object {
        fun byCode(code: String): Currency? = entries.firstOrNull { it.code == code }
    }
}
