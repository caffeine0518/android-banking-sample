package com.study.bank.feature.transfer.accountinput.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface AccountInputEffect {
    data object NavigateBack : AccountInputEffect

    /**
     * 수취계좌가 실명조회로 확정되어 다음 단계(금액 입력)로 진행.
     * 실명조회로 확정된 수취인 신원([recipient])을 실어, 출금계좌 저장소에 없는 외부 계좌도 재조회 없이 흐른다.
     */
    data class NavigateToAmount(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
    ) : AccountInputEffect
}
