package com.study.bank.feature.transfer.confirm.contract

sealed interface ConfirmEffect {
    data object NavigateBack : ConfirmEffect

    /** "받는 분에게 표시" 편집 화면(별도). 미구현이라 현재는 placeholder. */
    data object EditDisplayName : ConfirmEffect

    /** 출금계좌 변경 화면(별도). 미구현이라 현재는 placeholder. */
    data object ChangeSource : ConfirmEffect

    /**
     * "보내기" 확정 → 다음 단계(비밀번호/송금 실행·완료)로 진행. 해당 화면 미구현이라 현재는
     * placeholder로 연결만 되어 있고, 구현 시 송금 payload(출금·수취·금액)를 실어 보낸다.
     */
    data object Submit : ConfirmEffect
}
