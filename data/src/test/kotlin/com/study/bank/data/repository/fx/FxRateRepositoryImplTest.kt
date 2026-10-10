package com.study.bank.data.repository.fx

import com.study.bank.data.remote.fx.api.KeximApiService
import com.study.bank.data.remote.fx.api.KeximRates
import com.study.bank.data.remote.fx.dto.KeximRateItem
import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FxRateRepositoryImplTest {

    // KEXIM 11:00 KST 발표 이후 시점으로 고정 — 자정 경계/시간대 의존을 제거.
    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-06-07T12:00:00Z"),
        ZoneId.of("Asia/Seoul"),
    )

    @Test
    fun `같은 KEXIM 응답을 다른 target으로 요청하면 각각 다른 뷰가 emit된다`() = runTest {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
            success("JPY(100)", "950.00"),
        )
        val api = FakeKeximApiService(mapOf(yesterday() to KeximRates.Published(items)))
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val krwView = repo.observeRates(Currency.KRW).first()
        val usdView = repo.observeRates(Currency.USD).first()
        val eurView = repo.observeRates(Currency.EUR).first()

        assertEquals(0, BigDecimal.ONE.compareTo(krwView[Currency.KRW]))
        assertEquals(0, BigDecimal.ONE.compareTo(usdView[Currency.USD]))
        assertEquals(0, BigDecimal.ONE.compareTo(eurView[Currency.EUR]))

        assertEquals(0, BigDecimal("1350").compareTo(krwView[Currency.USD]))
        // USD view의 KRW: 1 / 1350 = 0.00074074 (SCALE=8, HALF_UP)
        assertEquals(0, BigDecimal("0.00074074").compareTo(usdView[Currency.KRW]))
    }

    @Test
    fun `최근 응답이 비면 다음 날짜로 walkback`() = runTest {
        val api = FakeKeximApiService(mapOf(
            yesterday() to KeximRates.NotPublished,
            yesterday().minusDays(1) to KeximRates.NotPublished,
            yesterday().minusDays(2) to published(success("USD", "1,400.00")),
        ))
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        assertEquals(0, BigDecimal("1400").compareTo(result[Currency.USD]))
        // 날짜를 건너뛰지 않고 하루씩 거슬러 조회한다.
        assertEquals(3, api.callCount)
    }

    @Test
    fun `인증키 오류면 날짜를 바꿔 다시 조회하지 않고 identity만 반환한다`() = runTest {
        val api = FakeKeximApiService(ratesByDate = emptyMap(), default = KeximRates.InvalidKey)
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        assertEquals(mapOf(Currency.KRW to BigDecimal.ONE), result)
        assertEquals(1, api.callCount)
    }

    @Test
    fun `일일 한도 초과면 날짜를 바꿔 다시 조회하지 않고 identity만 반환한다`() = runTest {
        val api = FakeKeximApiService(ratesByDate = emptyMap(), default = KeximRates.LimitExceeded)
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        assertEquals(mapOf(Currency.KRW to BigDecimal.ONE), result)
        assertEquals(1, api.callCount)
    }

    @Test
    fun `walkback 한도까지 모두 실패하면 어떤 target이든 identity row만 반환`() = runTest {
        val api = FakeKeximApiService(ratesByDate = emptyMap())
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        listOf(Currency.KRW, Currency.USD, Currency.EUR).forEach { target ->
            val result = repo.observeRates(target).first()
            assertEquals("target=$target", 1, result.size)
            assertEquals(0, BigDecimal.ONE.compareTo(result[target]))
            assertNull(result[Currency.JPY])
        }
    }

    @Test
    fun `API 예외 발생해도 walkback으로 다음 날짜 시도`() = runTest {
        val api = FakeKeximApiService(
            ratesByDate = mapOf(yesterday().minusDays(1) to published(success("USD", "1,200.00"))),
            failOnDates = setOf(yesterday()),
        )
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        assertEquals(0, BigDecimal("1200").compareTo(result[Currency.USD]))
        assertTrue(api.callCount >= 2)
    }

    private fun yesterday() = LocalDate.now(fixedClock).minusDays(1)

    private fun success(curUnit: String, dealBasR: String) = KeximRateItem(
        result = 1,
        curUnit = curUnit,
        dealBasR = dealBasR,
    )

    private fun published(vararg items: KeximRateItem) = KeximRates.Published(items.toList())

    private class FakeKeximApiService(
        private val ratesByDate: Map<LocalDate, KeximRates>,
        private val failOnDates: Set<LocalDate> = emptySet(),
        private val default: KeximRates = KeximRates.NotPublished,
    ) : KeximApiService {

        var callCount: Int = 0
            private set

        override suspend fun getRates(date: LocalDate): KeximRates {
            callCount++
            if (date in failOnDates) throw RuntimeException("simulated network failure for $date")
            return ratesByDate[date] ?: default
        }
    }
}
