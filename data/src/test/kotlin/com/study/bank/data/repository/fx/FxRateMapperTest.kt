package com.study.bank.data.repository.fx

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

    // ----- KEXIM 입력 파싱 -----

    // KEXIM 통화별 응답이 누락 없이 도메인 통화 키로 흐르는지.
    @Test
    fun `map은 KEXIM 응답 한 통화당 한 행으로 변환`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
            success("JPY(100)", "950.00"),
        )

        val result = mapper.map(items, Currency.KRW)

        assertNotNull(result)
        assertTrue(result!!.containsKey(Currency.USD))
        assertTrue(result.containsKey(Currency.EUR))
        assertTrue(result.containsKey(Currency.JPY))
    }

    // KEXIM이 JPY를 100엔 단위로 주는 변칙을 매퍼가 흡수해야 함.
    @Test
    fun `map은 JPY(100) 단위를 1엔 단위로 정규화`() {
        val items = listOf(success("JPY(100)", "950.00"))

        val result = mapper.map(items, Currency.KRW)

        assertEquals(0, BigDecimal("9.5").compareTo(result!![Currency.JPY]))
    }

    // 인증실패/휴일 응답이 환율 0으로 환산에 끼는 사고 방지.
    @Test
    fun `map은 result가 1이 아닌 응답을 제외`() {
        val items = listOf(
            success("USD", "1,350.00"),
            KeximRateItem(result = 3, curUnit = "EUR", dealBasR = "1,450.00"),
            KeximRateItem(result = 2, curUnit = "JPY(100)", dealBasR = "950.00"),
        )

        val result = mapper.map(items, Currency.KRW)

        assertNotNull(result)
        assertTrue(result!!.containsKey(Currency.USD))
        assertNull(result[Currency.EUR])
        assertNull(result[Currency.JPY])
    }

    // 미지원 통화 추가 시 매핑이 깨지지 않는지.
    @Test
    fun `map은 도메인이 모르는 통화 코드를 무시`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("CNH", "190.00"),
            success("GBP", "1,700.00"),
        )

        val result = mapper.map(items, Currency.KRW)

        assertNotNull(result)
        assertTrue(result!!.containsKey(Currency.USD))
        assertEquals(2, result.size) // USD + KRW identity
    }

    // KEXIM 콤마 포함 숫자 포맷이 BigDecimal 파싱에서 깨지지 않는지.
    @Test
    fun `map은 천단위 콤마 포함 문자열을 파싱`() {
        val items = listOf(success("USD", "1,234,567.89"))

        val result = mapper.map(items, Currency.KRW)

        assertEquals(0, BigDecimal("1234567.89").compareTo(result!![Currency.USD]))
    }

    // ----- "데이터 없음" 시그널 -----

    // 빈 입력 또는 전부 실패 응답이면 호출자가 "데이터 없음"을 명시적으로 알 수 있어야 함.
    @Test
    fun `map은 파싱 가능한 응답이 없으면 null 반환`() {
        listOf(Currency.KRW, Currency.USD, Currency.EUR).forEach { target ->
            assertNull("empty + target=$target", mapper.map(emptyList(), target))
        }

        val allFailures = listOf(
            KeximRateItem(result = 3, curUnit = "USD", dealBasR = "1,350.00"),
            KeximRateItem(result = 2),
        )
        assertNull(mapper.map(allFailures, Currency.KRW))
    }

    // target 통화 데이터가 KEXIM에 없으면 silent 폴백 대신 null로 "환산 불가" 신호.
    @Test
    fun `map은 target 통화 데이터가 없으면 null 반환`() {
        // EUR만 있고 USD 없는 응답 + target USD
        val items = listOf(success("EUR", "1,450.00"))

        assertNull(mapper.map(items, Currency.USD))
    }

    // ----- target 동적성 (rebaser와의 통합) -----

    // target 파라미터가 KEXIM 파싱 결과를 통과해 rebaser까지 흐르는지 (와이어링 회귀 방지).
    @Test
    fun `map은 target에 따라 다른 통화 기준 환율을 emit`() {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
        )

        val toKrw = mapper.map(items, Currency.KRW)
        val toUsd = mapper.map(items, Currency.USD)

        // KRW view: 1 USD = 1350 KRW
        assertEquals(0, BigDecimal("1350").compareTo(toKrw!![Currency.USD]))
        // USD view: 1 USD = 1 USD (identity), 1 EUR = 1450/1350 ≈ 1.074 USD
        assertEquals(0, BigDecimal.ONE.compareTo(toUsd!![Currency.USD]))
        assertTrue(toUsd[Currency.EUR]!! > BigDecimal("1.07") && toUsd[Currency.EUR]!! < BigDecimal("1.08"))
    }

    private fun success(curUnit: String, dealBasR: String) = KeximRateItem(
        result = 1,
        curUnit = curUnit,
        dealBasR = dealBasR,
    )
}
