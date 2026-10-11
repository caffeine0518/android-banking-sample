package com.study.bank.data.remote.kftc.mock.model

@JvmInline
internal value class AccountNumDigits private constructor(val value: String) {

    companion object {
        fun of(accountNum: String): AccountNumDigits = AccountNumDigits(accountNum.filter(Char::isDigit))
    }
}
