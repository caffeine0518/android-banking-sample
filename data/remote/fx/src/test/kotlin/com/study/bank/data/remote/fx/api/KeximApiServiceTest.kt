package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * KEXIM 환율 API 실제 호출 통합 테스트.
 *
 * MockWebServer 대신 oapi.koreaexim.go.kr 직접 호출. 네트워크 필요 + 일 1,000회 한도 공유.
 * 인증키는 BuildConfig.KEXIM_API_KEY로 주입 (build.gradle.kts에 하드코딩).
 *
 * 영업일/연휴 회피를 위해 [fetchRecentSuccess]가 어제부터 walkback하며 result=1 응답 찾을 때까지 시도.
 */
class KeximApiServiceTest {

    private lateinit var api: KeximApiService

    @Before
    fun setUp() {
        api = createKeximApiService()
    }

    // 실제 KEXIM 호출 — USD 매매기준율/매도/매수가 모두 채워지고 FX 호가 불변량(매도 > 매매기준율 > 매입)을 만족.
    @Test
    fun `실제 KEXIM 호출은 USD 매매기준율과 매도 매입율을 채우고 호가 불변량을 만족한다`() = runTest {
        val items = fetchRecentSuccess()
        val usd = items.firstOrNull { it.curUnit == "USD" }
        assertNotNull("응답에 USD 항목이 있어야 한다", usd)
        usd!!

        assertEquals(1, usd.result)
        assertEquals("미국 달러", usd.curNm)
        assertNotNull("매매기준율 채워짐", usd.dealBasR)
        assertNotNull("TTS(매도율) 채워짐", usd.tts)
        assertNotNull("TTB(매입율) 채워짐", usd.ttb)
        assertNotNull("KFTC 매매기준율 채워짐", usd.kftcDealBasR)

        val bid = parseRate(usd.ttb!!)
        val mid = parseRate(usd.dealBasR!!)
        val ask = parseRate(usd.tts!!)
        assertTrue("매도(살때) > 매매기준율: $ask vs $mid", ask > mid)
        assertTrue("매매기준율 > 매입(팔때): $mid vs $bid", mid > bid)
    }

    // 잘못된 인증키 → result=3 + 다른 필드 null. 키 없이도 돌아가는 테스트(인증 실패 경로 검증).
    @Test
    fun `잘못된 인증키로 호출하면 result 3과 null 필드들로 응답한다`() = runTest {
        val brokenApi = createKeximApiService(authKey = "INVALID_KEY_FOR_TEST")

        val items = brokenApi.getRates(lastBusinessDay())

        assertEquals(1, items.size)
        val item = items.single()
        assertEquals(3, item.result)
        assertNull(item.curUnit)
        assertNull(item.dealBasR)
    }

    // 미래 날짜 → result != 1. KEXIM은 빈 배열 또는 result=2 항목으로 응답한다고 알려져 있어 양쪽 모두 허용.
    @Test
    fun `미래 날짜로 호출하면 정상(result 1) 응답을 받지 않는다`() = runTest {
        val items = api.getRates(LocalDate.now().plusYears(10))

        if (items.isNotEmpty()) {
            assertTrue("미래 날짜는 result != 1이어야 한다: ${items.first().result}",
                items.first().result != 1)
        }
        // 빈 배열도 정상으로 인정 (KEXIM의 비영업일 응답 패턴 중 하나).
    }

    /**
     * 어제부터 [MAX_WALKBACK]일 전까지 차례로 호출하며 result=1 응답을 만나면 반환.
     * KEXIM은 약 11:00 KST에 당일 데이터 게시 + 연휴/주말 미게시 → walkback 필요.
     */
    private suspend fun fetchRecentSuccess(): List<KeximRateItem> {
        var date = LocalDate.now().minusDays(1)
        repeat(MAX_WALKBACK) {
            val items = api.getRates(date)
            if (items.isNotEmpty() && items.first().result == 1) return items
            date = date.minusDays(1)
        }
        error("최근 ${MAX_WALKBACK}일 안에 KEXIM 정상 응답 없음 — 장기 연휴/KEXIM 장애/키 문제 의심")
    }

    /** 어제부터 walkback하여 주말 회피한 가장 최근 평일. 공휴일은 무시(테스트가 그날 깨질 수 있음). */
    private fun lastBusinessDay(): LocalDate {
        var d = LocalDate.now().minusDays(1)
        while (d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) {
            d = d.minusDays(1)
        }
        return d
    }

    /** "1,538.29" → 1538.29 (천단위 쉼표 제거 후 parse). */
    private fun parseRate(raw: String): Double = raw.replace(",", "").toDouble()

    private companion object {
        const val MAX_WALKBACK = 10
    }
}
