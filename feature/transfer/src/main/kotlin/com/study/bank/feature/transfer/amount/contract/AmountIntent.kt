package com.study.bank.feature.transfer.amount.contract

import com.study.bank.domain.model.account.Account

sealed interface AmountAction

sealed interface AmountIntent : AmountAction {
    data object BackClicked : AmountIntent

    /** 키패드 숫자 입력. "1".."9", "0", "00". */
    data class DigitAppended(val digit: String) : AmountIntent

    /** 키패드 지우기(한 자리 삭제). */
    data object BackspacePressed : AmountIntent

    /** "잔액 · N원 입력" 칩 → 잔액 전액 입력. */
    data object FillBalanceClicked : AmountIntent

    /** "다음" 버튼 → 다음 단계로 진행. */
    data object NextClicked : AmountIntent
}

internal sealed interface AmountInternalAction : AmountAction {
    /** 출금계좌 잔액 갱신. 수취인은 라우트로 확정돼 고정이라 여기 싣지 않는다. */
    data class SourceUpdated(val source: Account?) : AmountInternalAction
}
