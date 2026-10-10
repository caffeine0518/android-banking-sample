package com.study.bank.feature.transfer.amount.contract

import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi

data class AmountState(
    val source: AmountSourceUi? = null,
    val recipient: AmountRecipientUi? = null,
    /** 출금계좌 통화의 최소 단위 정수(USD 10050 = $100.50). 잔액을 넘지 않도록 클램프된다. */
    val amount: Long = 0L,
) {
    val isAmountEntered: Boolean get() = amount > 0L
}
