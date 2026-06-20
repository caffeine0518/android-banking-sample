package com.study.bank.feature.transfer.confirm.contract

sealed interface ConfirmEffect {
    data object NavigateBack : ConfirmEffect

    /** "받는 분에게 표시" 편집 화면(별도). 미구현이라 현재는 placeholder. */
    data object EditDisplayName : ConfirmEffect

    /** 출금계좌 변경 화면(별도). 미구현이라 현재는 placeholder. */
    data object ChangeSource : ConfirmEffect

    /**
     * "보내기" 확정 → 송금 결과 화면으로 진행하며 실제 송금을 실행하게 한다.
     * 출금·수취 식별자와 금액을 실어 결과 화면이 송금 요청을 구성할 수 있게 한다.
     */
    data class Submit(
        val sourceAccountId: String,
        val recipientAccountId: String,
        val amount: Long,
    ) : ConfirmEffect
}
