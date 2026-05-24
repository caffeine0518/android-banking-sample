package com.study.bank.domain.model.transfer

sealed interface RecipientValidation {
    data class Valid(val holderName: String) : RecipientValidation
    data object NotFound : RecipientValidation
    data object Inactive : RecipientValidation
    data object SelfTransfer : RecipientValidation
}
