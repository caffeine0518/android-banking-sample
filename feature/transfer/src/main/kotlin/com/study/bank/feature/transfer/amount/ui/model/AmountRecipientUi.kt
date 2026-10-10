package com.study.bank.feature.transfer.amount.ui.model

/** 외부 계좌도 표현해야 해서 별명·계좌 종류 대신 실명조회한 예금주명을 쓴다. */
data class AmountRecipientUi(
    val holderName: String,
    val bankDisplayName: String,
    val accountNumber: String,
)
