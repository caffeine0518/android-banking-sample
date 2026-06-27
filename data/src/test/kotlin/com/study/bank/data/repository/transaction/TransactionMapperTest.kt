package com.study.bank.data.repository.transaction

import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionMapperTest {

    private val mapper = TransactionMapper()
    private val accountId = AccountId("120220112345678901234001")

    @Test
    fun `입금+이체는 TRANSFER_IN으로 매핑된다`() {
        assertEquals(
            TransactionType.TRANSFER_IN,
            mapper.map(item(inout = "입금", tranType = "이체"), accountId, Currency.KRW).type,
        )
    }

    @Test
    fun `출금+이체는 TRANSFER_OUT으로 매핑된다`() {
        assertEquals(
            TransactionType.TRANSFER_OUT,
            mapper.map(item(inout = "출금", tranType = "이체"), accountId, Currency.KRW).type,
        )
    }

    @Test
    fun `입금-비이체는 DEPOSIT, 출금-비이체는 WITHDRAWAL`() {
        assertEquals(
            TransactionType.DEPOSIT,
            mapper.map(item(inout = "입금", tranType = "현금"), accountId, Currency.KRW).type,
        )
        assertEquals(
            TransactionType.WITHDRAWAL,
            mapper.map(item(inout = "출금", tranType = "현금"), accountId, Currency.KRW).type,
        )
    }

    @Test
    fun `금액과 잔액은 주어진 통화로 매핑된다`() {
        val t = mapper.map(item(amt = "100.00", after = "3145.80"), accountId, Currency.USD)

        assertEquals(Currency.USD, t.amount.currency)
        assertEquals(0, t.amount.amount.compareTo(BigDecimal("100.00")))
        assertEquals(Currency.USD, t.balanceAfter.currency)
        assertEquals(0, t.balanceAfter.amount.compareTo(BigDecimal("3145.80")))
    }

    @Test
    fun `occurred_at은 KST 날짜시간을 Instant로 파싱한다`() {
        // 2026-06-18 10:30:00 KST = 01:30:00 UTC
        val t = mapper.map(item(date = "20260618", time = "103000"), accountId, Currency.KRW)

        assertEquals(Instant.parse("2026-06-18T01:30:00Z"), t.occurredAt)
    }

    @Test
    fun `print_content는 counterparty 이름이 되고 빈 값이면 null`() {
        assertEquals(
            "세이프박스로",
            mapper.map(item(print = "세이프박스로"), accountId, Currency.KRW).counterparty?.name,
        )
        assertNull(mapper.map(item(print = ""), accountId, Currency.KRW).counterparty)
    }

    @Test
    fun `id는 계좌+일시+seq로 합성돼 같은 시각이라도 seq가 다르면 유니크하다`() {
        val a = mapper.map(item(date = "20260618", time = "103000", seq = 1), accountId, Currency.KRW)
        val b = mapper.map(item(date = "20260618", time = "103000", seq = 2), accountId, Currency.KRW)

        assertNotEquals(a.id.value, b.id.value)
        assertTrue(a.id.value.startsWith(accountId.value))
    }

    @Test
    fun `같은 seq는 페이징 위치와 무관하게 동일 id로 합성된다`() {
        val sample = item(date = "20260618", time = "103000", seq = 7)

        assertEquals(
            mapper.map(sample, accountId, Currency.KRW).id,
            mapper.map(sample, accountId, Currency.KRW).id,
        )
    }

    @Test
    fun `status는 항상 COMPLETED`() {
        assertEquals(TransactionStatus.COMPLETED, mapper.map(item(), accountId, Currency.KRW).status)
    }

    private fun item(
        date: String = "20260618",
        time: String = "103000",
        inout: String = "출금",
        tranType: String = "이체",
        print: String = "상대방",
        amt: String = "50000",
        after: String = "2797320",
        seq: Long = 1,
    ) = TransactionItemDto(
        tranSeq = seq,
        tranDate = date,
        tranTime = time,
        inoutType = inout,
        tranType = tranType,
        printContent = print,
        tranAmt = amt,
        afterBalanceAmt = after,
    )
}
