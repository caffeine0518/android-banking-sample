package com.study.bank.feature.transfer.amount.contract

import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi

/**
 * 금액 입력 화면 상태.
 *
 * [amount]는 출금계좌 통화의 최소단위(minor unit) 정수 금액(소수점 없는 키패드 입력).
 * 통화 exponent만큼 소수점을 밀어 표시·환산한다. 예) USD 10050 → $100.50, KRW 100 → ₩100.
 * 출금계좌 잔액(최소단위 환산)을 넘지 못하도록 reducer에서 클램프된다.
 */
data class AmountState(
    val source: AmountSourceUi? = null,
    val recipient: AmountRecipientUi? = null,
    val amount: Long = 0L,
) {
    /** 금액이 입력되면 "다음" 버튼 노출 + 잔액 입력 칩 숨김의 기준. */
    val isAmountEntered: Boolean get() = amount > 0L
}
