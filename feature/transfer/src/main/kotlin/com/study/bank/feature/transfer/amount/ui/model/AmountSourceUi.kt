package com.study.bank.feature.transfer.amount.ui.model

import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi

/** 출금계좌("...에서") 표시용. 잔액은 헤더 노출 + 입력 금액 상한(클램프)에 쓰인다. */
data class AmountSourceUi(
    val nickname: String?,
    val type: AccountTypeUi,
    val balance: MoneyUi,
)
