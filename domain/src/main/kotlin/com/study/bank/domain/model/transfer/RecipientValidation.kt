package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.account.AccountId

sealed interface RecipientValidation {
    /**
     * 송금 가능한 수취인. [accountId]는 실명조회로 해석된 수취계좌 식별자로,
     * 금액 입력 화면 진입(수취계좌 조회)에 사용한다.
     */
    data class Valid(val accountId: AccountId, val holderName: String) : RecipientValidation
    data object NotFound : RecipientValidation
    data object Inactive : RecipientValidation
    data object SelfTransfer : RecipientValidation
}
