package com.study.bank.data.repository.fx

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CurrencyRebaserTest {

    private lateinit var rebaser: CurrencyRebaser

    @Before
    fun setUp() {
        rebaser = CurrencyRebaser()
    }

    // 사용자 로케일과 무관하게 항상 성립할 핵심 invariant.
    @Test
    fun `어떤 target이든 자기 자신을 identity로 반환`() {
        val anchored = sampleAnchored()

        listOf(Currency.KRW, Currency.USD, Currency.EUR, Currency.JPY).forEach { target ->
            val result = rebaser.rebase(anchored, target)
            assertEquals("target $target identity", 0, BigDecimal.ONE.compareTo(result[target]))
        }
    }

    // 단방향 환산만 옳고 역방향이 깨지는 수학 오류 방지.
    @Test
    fun `source-target 역수 관계를 만족`() {
        val anchored = sampleAnchored()

        val toUsd = rebaser.rebase(anchored, Currency.USD)
        val toEur = rebaser.rebase(anchored, Currency.EUR)

        // EUR/USD * USD/EUR ≈ 1
        val product = toUsd[Currency.EUR]!!.multiply(toEur[Currency.USD]!!)
        assertTrue(
            "EUR/USD × USD/EUR should be ~1, got $product",
            product > BigDecimal("0.999") && product < BigDecimal("1.001"),
        )
    }

    // contract: target 없는 anchored로 호출하면 silent 폴백 대신 즉시 실패해야 호출자가 알 수 있음.
    @Test
    fun `target이 입력에 없으면 IllegalArgumentException`() {
        val partial = mapOf(
            Currency.KRW to BigDecimal.ONE,
            Currency.USD to BigDecimal("1350"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            rebaser.rebase(partial, Currency.EUR)
        }
    }

    // SCALE=8 / HALF_UP 정책이 적용되는지 (반올림 정책 변경 회귀 방지).
    @Test
    fun `8자리 HALF_UP으로 라운딩`() {
        // 1 / 3 = 0.33333333... (9번째 자리 3 → 버림) → 0.33333333
        val baseDown = mapOf(Currency.KRW to BigDecimal.ONE, Currency.USD to BigDecimal(3))
        val resultDown = rebaser.rebase(baseDown, Currency.USD)
        assertEquals(0, BigDecimal("0.33333333").compareTo(resultDown[Currency.KRW]))

        // 1 / 1.5 = 0.66666666... (9번째 자리 6 → 올림) → 0.66666667
        val baseUp = mapOf(Currency.KRW to BigDecimal.ONE, Currency.USD to BigDecimal("1.5"))
        val resultUp = rebaser.rebase(baseUp, Currency.USD)
        assertEquals(0, BigDecimal("0.66666667").compareTo(resultUp[Currency.KRW]))
    }

    private fun sampleAnchored(): Map<Currency, BigDecimal> = mapOf(
        Currency.KRW to BigDecimal.ONE,
        Currency.USD to BigDecimal("1350"),
        Currency.EUR to BigDecimal("1450"),
        Currency.JPY to BigDecimal("9.5"),
    )
}
