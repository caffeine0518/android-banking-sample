package com.study.bank.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrencyTest {

    @Test
    fun `KRW has zero exponent`() {
        assertEquals("KRW", Currency.KRW.code)
        assertEquals(0, Currency.KRW.exponent)
    }

    @Test
    fun `USD has two exponent`() {
        assertEquals("USD", Currency.USD.code)
        assertEquals(2, Currency.USD.exponent)
    }

    @Test
    fun `JPY has zero exponent`() {
        assertEquals(0, Currency.JPY.exponent)
    }

    @Test
    fun `EUR has two exponent`() {
        assertEquals(2, Currency.EUR.exponent)
    }

    @Test
    fun `byCode resolves supported currency`() {
        assertEquals(Currency.KRW, Currency.byCode("KRW"))
        assertEquals(Currency.USD, Currency.byCode("USD"))
    }

    @Test
    fun `byCode returns null for unknown code`() {
        assertNull(Currency.byCode("XYZ"))
    }

    // 도메인이 명시한 단일 fallback 정책 — 모든 피쳐가 같은 기본값을 공유해야 함.
    @Test
    fun `DEFAULT is USD`() {
        assertEquals(Currency.USD, Currency.DEFAULT)
    }

    // 통화 해석 실패(null/미지원) 시 도메인 정책에 따라 폴백되는지.
    @Test
    fun `byCodeOrDefault falls back to DEFAULT for null or unknown`() {
        assertEquals(Currency.DEFAULT, Currency.byCodeOrDefault(null))
        assertEquals(Currency.DEFAULT, Currency.byCodeOrDefault("XYZ"))
    }

    // 정상 코드는 byCode와 동일하게 매칭되는지.
    @Test
    fun `byCodeOrDefault returns matched currency for known code`() {
        assertEquals(Currency.KRW, Currency.byCodeOrDefault("KRW"))
        assertEquals(Currency.EUR, Currency.byCodeOrDefault("EUR"))
    }
}
