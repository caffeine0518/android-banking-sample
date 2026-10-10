package com.study.bank.data.repository.fx

import com.study.bank.data.remote.fx.api.KeximRates
import com.study.bank.data.remote.fx.dto.KeximRateItem
import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FxRateMapperTest {

    private lateinit var mapper: FxRateMapper

    @Before
    fun setUp() {
        mapper = FxRateMapper(CurrencyRebaser())
    }

    @Test
    fun `map은 KEXIM 응답 한 통화당 한 행으로 변환`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
            success("JPY(100)", "950.00"),
        )

        val result = mapper.map(KeximRates.Published(items), Currency.KRW)

        assertNotNull(result)
        assertTrue(result!!.containsKey(Currency.USD))
        assertTrue(result.containsKey(Currency.EUR))
        assertTrue(result.containsKey(Currency.JPY))
    }

    @Test
    fun `map은 JPY(100) 단위를 1엔 단위로 정규화`() {
        val items = listOf(success("JPY(100)", "950.00"))

        val result = mapper.map(KeximRates.Published(items), Currency.KRW)

        assertEquals(0, BigDecimal("9.5").compareTo(result!![Currency.JPY]))
    }

    @Test
    fun `map은 도메인이 모르는 통화 코드를 무시`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("CNH", "190.00"),
            success("GBP", "1,700.00"),
        )

        val result = mapper.map(KeximRates.Published(items), Currency.KRW)

        assertNotNull(result)
        assertTrue(result!!.containsKey(Currency.USD))
        assertEquals(2, result.size) // USD + KRW identity
    }

    @Test
    fun `map은 천단위 콤마 포함 문자열을 파싱`() {
        val items = listOf(success("USD", "1,234,567.89"))

        val result = mapper.map(KeximRates.Published(items), Currency.KRW)

        assertEquals(0, BigDecimal("1234567.89").compareTo(result!![Currency.USD]))
    }

    @Test
    fun `map은 파싱 가능한 응답이 없으면 null 반환`() {
        listOf(Currency.KRW, Currency.USD, Currency.EUR).forEach { target ->
            assertNull("empty + target=$target", mapper.map(KeximRates.Published(emptyList()), target))
        }
    }

    @Test
    fun `map은 target 통화 데이터가 없으면 null 반환`() {
        val items = listOf(success("EUR", "1,450.00"))

        assertNull(mapper.map(KeximRates.Published(items), Currency.USD))
    }

    @Test
    fun `map은 target에 따라 다른 통화 기준 환율을 emit`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
        )

        val toKrw = mapper.map(KeximRates.Published(items), Currency.KRW)
        val toUsd = mapper.map(KeximRates.Published(items), Currency.USD)

        // KRW 기준: 1 USD = 1350 KRW
        assertEquals(0, BigDecimal("1350").compareTo(toKrw!![Currency.USD]))
        // USD 기준: 1 USD = 1 USD(항등), 1 EUR = 1450/1350 ≈ 1.074 USD
        assertEquals(0, BigDecimal.ONE.compareTo(toUsd!![Currency.USD]))
        assertTrue(toUsd[Currency.EUR]!! > BigDecimal("1.07") && toUsd[Currency.EUR]!! < BigDecimal("1.08"))
    }

    private fun success(curUnit: String, dealBasR: String) = KeximRateItem(
        result = 1,
        curUnit = curUnit,
        dealBasR = dealBasR,
    )
}
