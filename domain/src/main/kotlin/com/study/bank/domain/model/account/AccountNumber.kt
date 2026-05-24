package com.study.bank.domain.model.account

@JvmInline
value class AccountNumber(val value: String) {

    init {
        require(value.isNotBlank()) {
            "Account number cannot be blank"
        }
        require(value.all(Char::isDigit)) {
            "Account number must contain digits only"
        }
        require(value.length in MIN_DIGITS..MAX_DIGITS) {
            "Account number must have $MIN_DIGITS..$MAX_DIGITS digits"
        }
    }

    companion object {
        private const val MIN_DIGITS = 10
        private const val MAX_DIGITS = 14
    }
}
