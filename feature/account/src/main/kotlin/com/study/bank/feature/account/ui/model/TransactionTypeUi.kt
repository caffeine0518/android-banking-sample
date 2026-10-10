package com.study.bank.feature.account.ui.model

enum class TransactionTypeUi {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
    ;

    val isInbound: Boolean
        get() = this == DEPOSIT || this == TRANSFER_IN
}
