package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber

data class TransferRequest(
    val fromAccountId: AccountId,
    val toAccountNumber: AccountNumber,
    val toBankCode: BankCode,
    val amount: Money,
    val memo: String?,
    val idempotencyKey: String,
) {
    init {
        require(amount > Money.ZERO) { "Transfer amount must be positive, got $amount" }
        require(idempotencyKey.isNotBlank()) { "Idempotency key cannot be blank" }
    }
}
