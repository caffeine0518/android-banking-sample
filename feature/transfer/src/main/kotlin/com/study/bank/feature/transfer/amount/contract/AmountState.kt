package com.study.bank.feature.transfer.amount.contract

import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi

/**
 * 금액 입력 화면 상태.
 *
 * [amount]는 출금계좌 통화의 정수 금액(원 단위 키패드 입력). 출금계좌 잔액을 넘지 못하도록
 * reducer에서 클램프된다.
 */
data class AmountState(
    val source: AmountSourceUi? = null,
    val recipient: AmountRecipientUi? = null,
    val amount: Long = 0L,
) {
    /** 금액이 입력되면 "다음" 버튼 노출 + 잔액 입력 칩 숨김의 기준. */
    val isAmountEntered: Boolean get() = amount > 0L
}
