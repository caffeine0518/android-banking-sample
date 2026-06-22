package com.study.bank.feature.transfer.amount.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface AmountEffect {
    data object NavigateBack : AmountEffect

    /**
     * 금액이 확정되어 다음 단계(송금 확인)로 진행. 수취인 신원과 금액을 실어 확인 화면이 그대로 이어받게 한다.
     * [amount]는 출금계좌 통화의 최소단위(minor unit) 정수.
     */
    data class NavigateNext(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
        val amount: Long,
    ) : AmountEffect
}
