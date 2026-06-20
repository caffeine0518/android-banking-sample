package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TransferRequestTest {

    private fun validRequest() = TransferRequest(
        fromAccountId = AccountId("acc-1"),
        senderName = "홍길동",
        toAccountNumber = AccountNumber("100012345678"),
        toBankCode = BankCode.TOSS,
        recipientName = "김토스",
        amount = Money.of(10_000, Currency.KRW),
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
            validRequest().copy(amount = Money.zero(Currency.KRW))
        }
    }

    @Test
    fun `blank idempotency key is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            validRequest().copy(idempotencyKey = "   ")
        }
    }

    @Test
    fun `blank sender name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            validRequest().copy(senderName = "   ")
        }
    }

    @Test
    fun `blank recipient name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            validRequest().copy(recipientName = "")
        }
    }
}
