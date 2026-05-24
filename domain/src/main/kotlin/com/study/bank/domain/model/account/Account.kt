package com.study.bank.domain.model.account

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Money

data class Account(
    val id: AccountId,
    val number: AccountNumber,
    val bankCode: BankCode,
    val holderName: String,
    val balance: Money,
    val type: AccountType,
    val nickname: String? = null,
)
