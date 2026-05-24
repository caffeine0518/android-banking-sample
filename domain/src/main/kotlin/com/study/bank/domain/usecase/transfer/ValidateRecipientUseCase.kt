package com.study.bank.domain.usecase.transfer

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup
import com.study.bank.domain.model.transfer.RecipientValidation
import com.study.bank.domain.repository.RecipientRepository

class ValidateRecipientUseCase(
    private val recipientRepository: RecipientRepository,
) {
    suspend operator fun invoke(
        fromAccountId: AccountId,
        toAccountNumber: AccountNumber,
        toBankCode: BankCode,
    ): RecipientValidation {
        val lookup = recipientRepository.lookup(toAccountNumber, toBankCode)
        return when (lookup) {
            is RecipientLookup.Active ->
                if (lookup.accountId == fromAccountId) {
                    RecipientValidation.SelfTransfer
                } else {
                    RecipientValidation.Valid(lookup.holderName)
                }

            is RecipientLookup.Inactive -> RecipientValidation.Inactive

            RecipientLookup.NotFound -> RecipientValidation.NotFound
        }
    }
}
