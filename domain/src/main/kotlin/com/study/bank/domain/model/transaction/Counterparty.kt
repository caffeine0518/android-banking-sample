package com.study.bank.domain.model.transaction

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountNumber

data class Counterparty(
    val name: String,
    val accountNumber: AccountNumber?,
    val bankCode: BankCode?,
)
