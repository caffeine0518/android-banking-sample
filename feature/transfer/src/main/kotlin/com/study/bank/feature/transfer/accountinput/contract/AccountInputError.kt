package com.study.bank.feature.transfer.accountinput.contract

enum class AccountInputError {
    NOT_FOUND,
    INACTIVE,
    SELF_TRANSFER,
    NETWORK,
    UNKNOWN,
}
