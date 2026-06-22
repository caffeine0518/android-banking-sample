package com.study.bank.feature.transfer.amount.contract

sealed interface AmountEffect {
    data object NavigateBack : AmountEffect

    /**
     * 금액이 확정되어 다음 단계(송금 확인)로 진행. 출금·수취 식별자와 금액을 실어
     * 확인 화면이 두 계좌와 금액을 조회할 수 있게 한다.
     * [amount]는 출금계좌 통화의 최소단위(minor unit) 정수.
     */
    data class NavigateNext(
        val sourceAccountId: String,
        val recipientAccountId: String,
        val amount: Long,
    ) : AmountEffect
}
