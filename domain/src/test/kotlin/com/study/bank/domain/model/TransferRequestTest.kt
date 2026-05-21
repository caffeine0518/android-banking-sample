package com.study.bank.domain.model

import kotlin.test.Test
import kotlin.test.assertFailsWith

class TransferRequestTest {

    private fun validRequest() = TransferRequest(
        fromAccountId = AccountId("acc-1"),
        toAccountNumber = AccountNumber("1000-1234-5678"),
        toBank = Bank.TOSS,
        amount = Money(10_000),
        memo = "월세",
        idempotencyKey = "key-1",
    )

    @Test
    fun `valid request is created`() {
        validRequest()
    }

    @Test
    fun `zero amount is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            validRequest().copy(amount = Money.ZERO)
        }
    }

    @Test
    fun `blank idempotency key is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            validRequest().copy(idempotencyKey = "   ")
        }
    }
}
