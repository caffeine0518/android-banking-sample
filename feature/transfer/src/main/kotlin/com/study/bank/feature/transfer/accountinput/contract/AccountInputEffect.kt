package com.study.bank.feature.transfer.accountinput.contract

sealed interface AccountInputEffect {
    data object NavigateBack : AccountInputEffect

    /**
     * 수취계좌가 실명조회로 확정되어 다음 단계(금액 입력)로 진행.
     * 출금계좌·수취계좌 식별자를 실어 금액 입력 화면이 두 계좌를 조회할 수 있게 한다.
     */
    data class NavigateToAmount(
        val sourceAccountId: String,
        val recipientAccountId: String,
    ) : AccountInputEffect
}
