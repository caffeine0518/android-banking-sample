package com.study.bank.domain.repository

import com.study.bank.domain.model.AccountNumber
import com.study.bank.domain.model.Bank
import com.study.bank.domain.model.RecipientValidation
import com.study.bank.domain.model.TransferOutcome
import com.study.bank.domain.model.TransferRequest

interface TransferRepository {

    suspend fun validateRecipient(
        accountNumber: AccountNumber,
        bank: Bank,
    ): RecipientValidation

    suspend fun execute(request: TransferRequest): TransferOutcome
}
