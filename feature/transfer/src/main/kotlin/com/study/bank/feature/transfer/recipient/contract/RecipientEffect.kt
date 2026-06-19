package com.study.bank.feature.transfer.recipient.contract

sealed interface RecipientEffect {
    data object NavigateBack : RecipientEffect

    /** "계좌번호 입력" 버튼 → 계좌번호 입력 화면(별도). 해당 화면 미구현이라 현재는 placeholder. */
    data object NavigateToAccountNumberInput : RecipientEffect

    /**
     * 수취 계좌(내 계좌)가 정해져 다음 단계(금액 입력)로 진행. 다음 화면 미구현이라 현재는 placeholder로
     * 연결만 되어 있고, 화면 구현 시 선택된 수취 정보를 payload로 싣는다.
     */
    data object Continue : RecipientEffect
}
