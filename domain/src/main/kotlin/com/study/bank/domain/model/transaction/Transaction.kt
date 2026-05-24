package com.study.bank.domain.model.transaction

import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import java.time.Instant

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
