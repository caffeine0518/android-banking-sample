package com.study.bank.data.repository.transaction

import com.study.bank.data.local.entity.TransactionEntity
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.Counterparty
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionEntityMapperTest {

    private val mapper = TransactionEntityMapper()

    @Test
    fun `toEntity-toDomain 라운드트립은 필드를 보존한다`() {
        val original = Transaction(
            id = TransactionId("tx-1"),
            accountId = AccountId("acc-1"),
            type = TransactionType.TRANSFER_OUT,
            amount = Money.of("50000", Currency.KRW),
            balanceAfter = Money.of("2797320", Currency.KRW),
            counterparty = Counterparty("세이프박스", null, null),
            memo = null,
            occurredAt = Instant.parse("2026-06-18T01:30:00Z"),
            status = TransactionStatus.COMPLETED,
        )

        val restored = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.id, restored.id)
        assertEquals(original.accountId, restored.accountId)
        assertEquals(original.type, restored.type)
        assertEquals(Currency.KRW, restored.amount.currency)
        assertEquals(0, restored.amount.amount.compareTo(BigDecimal("50000")))
        assertEquals(0, restored.balanceAfter.amount.compareTo(BigDecimal("2797320")))
        assertEquals("세이프박스", restored.counterparty?.name)
        assertEquals(original.occurredAt, restored.occurredAt)
        assertEquals(original.status, restored.status)
    }

    // B안 핵심: 우리가 저장한 값의 복원 실패 = 스키마-코드 정합성 붕괴 → check(IllegalStateException).
    @Test
    fun `알 수 없는 통화는 IllegalStateException으로 fail-fast`() {
        assertThrows(IllegalStateException::class.java) { mapper.toDomain(entity(currency = "XXX")) }
    }

    @Test
    fun `알 수 없는 타입은 IllegalStateException으로 fail-fast`() {
        assertThrows(IllegalStateException::class.java) { mapper.toDomain(entity(type = "BOGUS")) }
    }

    @Test
    fun `알 수 없는 상태는 IllegalStateException으로 fail-fast`() {
        assertThrows(IllegalStateException::class.java) { mapper.toDomain(entity(status = "BOGUS")) }
    }

    private fun entity(
        type: String = "TRANSFER_OUT",
        currency: String = "KRW",
        status: String = "COMPLETED",
    ) = TransactionEntity(
        id = "tx-1",
        accountId = "acc-1",
        type = type,
        amount = "50000",
        currency = currency,
        balanceAfter = "2797320",
        counterpartyName = "세이프박스",
        memo = null,
        occurredAt = 0L,
        status = status,
    )
}
