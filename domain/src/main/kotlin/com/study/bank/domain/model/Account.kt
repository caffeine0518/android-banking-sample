package com.study.bank.domain.model

@JvmInline
value class AccountId(val value: String)

enum class AccountType {
    CHECKING,
    SAVINGS,
    DEPOSIT,
}

data class Account(
    val id: AccountId,
    val number: AccountNumber,
    val bank: Bank,
    val holderName: String,
    val balance: Money,
    val type: AccountType,
    val nickname: String? = null,
)
