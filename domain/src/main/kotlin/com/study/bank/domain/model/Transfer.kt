package com.study.bank.domain.model

import java.time.Instant

data class TransferRequest(
    val fromAccountId: AccountId,
    val toAccountNumber: AccountNumber,
    val toBank: Bank,
    val amount: Money,
    val memo: String?,
    val idempotencyKey: String,
) {
    init {
        require(amount > Money.ZERO) { "Transfer amount must be positive, got $amount" }
        require(idempotencyKey.isNotBlank()) { "Idempotency key cannot be blank" }
    }
}

data class TransferResult(
    val transactionId: TransactionId,
    val status: TransactionStatus,
    val balanceAfter: Money,
    val completedAt: Instant,
)

sealed interface TransferOutcome {
    data class Success(val result: TransferResult) : TransferOutcome

    sealed interface Failure : TransferOutcome {
        data object InsufficientFunds : Failure
        data object InvalidRecipient : Failure
        data class DailyLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class PerTransactionLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class Network(val cause: Throwable) : Failure
        data class Unknown(val cause: Throwable) : Failure
    }
}

sealed interface RecipientValidation {
    data class Valid(val holderName: String) : RecipientValidation
    data object NotFound : RecipientValidation
    data object Inactive : RecipientValidation
}
