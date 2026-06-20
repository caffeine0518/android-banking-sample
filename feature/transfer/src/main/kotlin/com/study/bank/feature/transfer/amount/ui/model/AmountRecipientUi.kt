package com.study.bank.feature.transfer.amount.ui.model

import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi

/** 수취계좌("내 ...로") 표시용. 현재 송금 플로우는 내 계좌 수취만 지원한다. */
data class AmountRecipientUi(
    val nickname: String?,
    val type: AccountTypeUi,
    val bankDisplayName: String,
    val numberMasked: String,
)
