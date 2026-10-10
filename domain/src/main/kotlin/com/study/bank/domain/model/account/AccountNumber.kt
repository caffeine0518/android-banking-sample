package com.study.bank.domain.model.account

@JvmInline
value class AccountNumber(val value: String) {

    init {
        require(value.isNotBlank()) {
            "Account number cannot be blank"
        }
        require(value.length <= MAX_LENGTH) {
            "Account number must not exceed $MAX_LENGTH characters"
        }
        require(value.none(Char::isWhitespace)) {
            "Account number must not contain whitespace"
        }
    }

    companion object {
        // IBAN 최대 34자에 여유를 둔다.
        private const val MAX_LENGTH = 50
    }
}
