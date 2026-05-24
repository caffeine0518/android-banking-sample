package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.Money
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import java.time.Instant

data class TransferResult(
    val transactionId: TransactionId,
    val status: TransactionStatus,
    val balanceAfter: Money,
    val completedAt: Instant,
)
