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

/** 실제 KEXIM을 호출한다. 네트워크가 필요하고 일 1,000회 한도를 앱과 공유한다. */
class KeximApiServiceTest {

    private lateinit var api: KeximApiService

    @Before
    fun setUp() {
        api = createKeximApiService()
    }

    @Test
    fun `실제 KEXIM 호출은 USD 매매기준율과 매도·매입률을 포함하고 매도율이 기준율보다, 기준율이 매입률보다 높다`() = runTest {
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

    // 유효한 인증키 없이도 실행된다.
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

    // KEXIM은 미래 날짜에 빈 배열이나 result=2 항목으로 응답하므로 둘 다 허용한다.
    @Test
    fun `미래 날짜로 호출하면 정상(result 1) 응답을 받지 않는다`() = runTest {
        val items = api.getRates(LocalDate.now().plusYears(10))

        if (items.isNotEmpty()) {
            assertTrue("미래 날짜는 result != 1이어야 한다: ${items.first().result}",
                items.first().result != 1)
        }
    }

    /** KEXIM은 당일 데이터를 약 11:00 KST에 게시하고 주말·연휴에는 게시하지 않아 정상 응답이 나올 때까지 하루씩 거슬러 조회한다. */
    private suspend fun fetchRecentSuccess(): List<KeximRateItem> {
        var date = LocalDate.now().minusDays(1)
        repeat(MAX_WALKBACK) {
            val items = api.getRates(date)
            if (items.isNotEmpty() && items.first().result == 1) return items
            date = date.minusDays(1)
        }
        error("최근 ${MAX_WALKBACK}일 안에 KEXIM 정상 응답 없음 — 장기 연휴/KEXIM 장애/키 문제 의심")
    }

    /** 주말만 건너뛴다. 공휴일이면 실패할 수 있다. */
    private fun lastBusinessDay(): LocalDate {
        var d = LocalDate.now().minusDays(1)
        while (d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) {
            d = d.minusDays(1)
        }
        return d
    }

    private fun parseRate(raw: String): Double = raw.replace(",", "").toDouble()

    private companion object {
        const val MAX_WALKBACK = 10
    }
}
