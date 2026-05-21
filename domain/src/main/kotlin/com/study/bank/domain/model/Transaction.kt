package com.study.bank.domain.model

import java.time.Instant

@JvmInline
value class TransactionId(val value: String)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED,
}

data class Counterparty(
    val name: String,
    val accountNumber: AccountNumber?,
    val bank: Bank?,
)

data class Transaction(
    val id: TransactionId,
    val accountId: AccountId,
    val type: TransactionType,
    val amount: Money,
    val balanceAfter: Money,
    val counterparty: Counterparty?,
    val memo: String?,
    val occurredAt: Instant,
    val status: TransactionStatus,
)
