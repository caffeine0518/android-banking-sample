package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.account.AccountId

sealed interface RecipientValidation {
    data class Valid(val accountId: AccountId, val holderName: String) : RecipientValidation
    data object NotFound : RecipientValidation
    data object Inactive : RecipientValidation
    data object SelfTransfer : RecipientValidation
}
