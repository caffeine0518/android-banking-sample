package com.study.bank.data.remote.kftc.mock.model

internal infix fun String.sameDigitsAs(other: String): Boolean =
    filter(Char::isDigit) == other.filter(Char::isDigit)
