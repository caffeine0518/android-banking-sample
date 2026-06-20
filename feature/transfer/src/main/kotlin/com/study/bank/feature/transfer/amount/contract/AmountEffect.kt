package com.study.bank.feature.transfer.amount.contract

sealed interface AmountEffect {
    data object NavigateBack : AmountEffect

    /**
     * 금액이 확정되어 다음 단계(송금 확인)로 진행. 다음 화면 미구현이라 현재는 placeholder로
     * 연결만 되어 있고, 화면 구현 시 송금 payload(출금·수취·금액)를 싣는다.
     */
    data object NavigateNext : AmountEffect
}
