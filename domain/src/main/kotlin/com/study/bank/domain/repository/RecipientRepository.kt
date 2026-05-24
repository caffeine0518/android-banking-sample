package com.study.bank.domain.repository

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup

interface RecipientRepository {
    suspend fun lookup(
        accountNumber: AccountNumber,
        bankCode: BankCode,
    ): RecipientLookup
}
