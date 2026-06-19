package com.study.bank.feature.account.ui.model

enum class TransactionTypeUi {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
    ;

    /** 잔액이 느는 거래(입금/이체 입금)인지. 부호/색상 표시에 쓰인다. */
    val isInbound: Boolean
        get() = this == DEPOSIT || this == TRANSFER_IN
}
