package com.study.bank.feature.transfer.recipient.contract

sealed interface RecipientEffect {
    data object NavigateBack : RecipientEffect

    /**
     * "계좌번호 입력" 버튼 → 계좌번호 입력 화면(별도).
     * 출금계좌 식별자를 실어 입력 화면이 실명조회·자기이체 판별에 쓸 수 있게 한다.
     */
    data class NavigateToAccountNumberInput(val sourceAccountId: String) : RecipientEffect

    /**
     * 수취 계좌(내 계좌)가 정해져 다음 단계(금액 입력)로 진행.
     * 출금계좌·수취계좌 식별자를 실어 금액 입력 화면이 두 계좌를 조회할 수 있게 한다.
     */
    data class NavigateToAmount(
        val sourceAccountId: String,
        val recipientAccountId: String,
    ) : RecipientEffect
}
