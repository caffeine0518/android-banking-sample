package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber

data class TransferRequest(
    val fromAccountId: AccountId,
    val senderName: String,
    val toAccountNumber: AccountNumber,
    val toBankCode: BankCode,
    val recipientName: String,
    val amount: Money,
    val memo: String?,
    val idempotencyKey: String,
) {
    init {
        require(amount.isPositive()) { "Transfer amount must be positive, got $amount" }
        require(idempotencyKey.isNotBlank()) { "Idempotency key cannot be blank" }
        require(senderName.isNotBlank()) { "Sender name cannot be blank" }
        require(recipientName.isNotBlank()) { "Recipient name cannot be blank" }
    }
}
