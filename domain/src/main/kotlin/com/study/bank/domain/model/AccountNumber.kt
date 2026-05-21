package com.study.bank.domain.model

@JvmInline
value class AccountNumber(val value: String) {

    init {
        require(value.isNotBlank()) { "Account number cannot be blank" }
        require(value.all { it.isDigit() || it == '-' }) {
            "Account number contains invalid characters: $value"
        }
        val digits = value.count(Char::isDigit)
        require(digits in MIN_DIGITS..MAX_DIGITS) {
            "Account number must have $MIN_DIGITS..$MAX_DIGITS digits, got $digits"
        }
    }

    companion object {
        private const val MIN_DIGITS = 10
        private const val MAX_DIGITS = 14
    }
}
