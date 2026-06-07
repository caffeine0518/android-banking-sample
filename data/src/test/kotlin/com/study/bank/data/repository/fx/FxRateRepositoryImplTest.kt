package com.study.bank.data.repository.fx

import com.study.bank.data.remote.fx.api.KeximApiService
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

    // ----- target 통화에 따른 동적 환율 뷰 -----

    // repository가 target을 무시하고 KRW-base 응답 그대로 흘리는 회귀 방지.
    @Test
    fun `같은 KEXIM 응답을 다른 target으로 요청하면 각각 다른 뷰가 emit된다`() = runTest {
        val items = listOf(
            success("USD", "1,350.00"),
            success("EUR", "1,450.00"),
            success("JPY(100)", "950.00"),
        )
        val api = FakeKeximApiService(mapOf(yesterday() to items))
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val krwView = repo.observeRates(Currency.KRW).first()
        val usdView = repo.observeRates(Currency.USD).first()
        val eurView = repo.observeRates(Currency.EUR).first()

        // 각 뷰는 자기 target의 identity를 가진다
        assertEquals(0, BigDecimal.ONE.compareTo(krwView[Currency.KRW]))
        assertEquals(0, BigDecimal.ONE.compareTo(usdView[Currency.USD]))
        assertEquals(0, BigDecimal.ONE.compareTo(eurView[Currency.EUR]))

        // 그리고 cross-currency 환율은 target에 따라 다르다
        assertEquals(0, BigDecimal("1350").compareTo(krwView[Currency.USD]))
        // USD view의 KRW: 1 / 1350 = 0.00074074 (SCALE=8, HALF_UP)
        assertEquals(0, BigDecimal("0.00074074").compareTo(usdView[Currency.KRW]))
    }

    // ----- walkback (target과 무관한 영업일 회피 로직) -----

    // KEXIM이 휴일/주말에 빈 배열로 응답하는 실제 동작을 회피.
    @Test
    fun `최근 응답이 비면 다음 날짜로 walkback`() = runTest {
        val api = FakeKeximApiService(mapOf(
            yesterday() to emptyList(),
            yesterday().minusDays(1) to emptyList(),
            yesterday().minusDays(2) to listOf(success("USD", "1,400.00")),
        ))
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        // primary: walkback이 yesterday()-2 데이터에 도달했는지 결과로 증명
        assertEquals(0, BigDecimal("1400").compareTo(result[Currency.USD]))
        // secondary: 도중에 batch/skip 같은 알고리즘 변경 감지
        assertEquals(3, api.callCount)
    }

    // 빈 배열뿐 아니라 result=2(휴일) 응답도 walkback 트리거인지.
    @Test
    fun `result가 1이 아닌 응답도 walkback 대상`() = runTest {
        val api = FakeKeximApiService(mapOf(
            yesterday() to listOf(KeximRateItem(result = 2)), // holiday
            yesterday().minusDays(1) to listOf(success("USD", "1,500.00")),
        ))
        val repo = FxRateRepositoryImpl(api, FxRateMapper(CurrencyRebaser()), fixedClock)

        val result = repo.observeRates(Currency.KRW).first()

        // primary: 휴일 응답을 건너뛰고 -1일 데이터를 채택했는지 결과로 증명
        assertEquals(0, BigDecimal("1500").compareTo(result[Currency.USD]))
        // secondary: result=2가 정상 응답으로 오인되지 않았는지
        assertEquals(2, api.callCount)
    }

    // KEXIM 장기 장애 시 앱이 환율 없이도 동작 (UI가 빈 맵으로 NPE 안 나게).
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

    // 일회성 네트워크 에러가 전체 환율 흐름을 끊지 않게 (resilience).
    @Test
    fun `API 예외 발생해도 walkback으로 다음 날짜 시도`() = runTest {
        val api = FakeKeximApiService(
            ratesByDate = mapOf(yesterday().minusDays(1) to listOf(success("USD", "1,200.00"))),
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

    private class FakeKeximApiService(
        private val ratesByDate: Map<LocalDate, List<KeximRateItem>>,
        private val failOnDates: Set<LocalDate> = emptySet(),
    ) : KeximApiService {

        var callCount: Int = 0
            private set

        override suspend fun getRates(date: LocalDate): List<KeximRateItem> {
            callCount++
            if (date in failOnDates) throw RuntimeException("simulated network failure for $date")
            return ratesByDate[date] ?: emptyList()
        }
    }
}
