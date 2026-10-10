package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface ConfirmEffect {
    data object NavigateBack : ConfirmEffect

    /**
     * "보내기" 확정 → 송금 결과 화면으로 진행하며 실제 송금을 실행하게 한다.
     * 수취인 신원과 금액, 이 송금의 멱등성 키를 포함해 결과 화면이 송금 요청을 구성할 수 있게 한다.
     */
    data class Submit(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
        val amount: Long,
        val idempotencyKey: String,
    ) : ConfirmEffect
}
