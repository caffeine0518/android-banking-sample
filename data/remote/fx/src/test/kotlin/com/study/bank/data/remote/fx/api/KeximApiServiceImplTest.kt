package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeximApiServiceImplTest {

    @Test
    fun `result 1이면 Published로 반환하고 result가 1이 아닌 항목은 제외한다`() = runTest {
        val usd = KeximRateItem(result = 1, curUnit = "USD", dealBasR = "1,350.00")
        val service = serviceReturning(usd, KeximRateItem(result = 2, curUnit = "EUR"))

        assertEquals(KeximRates.Published(listOf(usd)), service.getRates(DATE))
    }

    @Test
    fun `빈 배열이거나 result 2면 NotPublished를 반환한다`() = runTest {
        assertEquals(KeximRates.NotPublished, serviceReturning().getRates(DATE))
        assertEquals(KeximRates.NotPublished, serviceReturning(KeximRateItem(result = 2)).getRates(DATE))
    }

    @Test
    fun `result 3이면 InvalidKey를 반환한다`() = runTest {
        assertEquals(KeximRates.InvalidKey, serviceReturning(KeximRateItem(result = 3)).getRates(DATE))
    }

    @Test
    fun `result 4면 LimitExceeded를 반환한다`() = runTest {
        assertEquals(KeximRates.LimitExceeded, serviceReturning(KeximRateItem(result = 4)).getRates(DATE))
    }

    private fun serviceReturning(vararg items: KeximRateItem) = KeximApiServiceImpl(
        httpApi = object : KeximHttpApi {
            override suspend fun getRates(authKey: String, searchDate: String, dataType: String) = items.toList()
        },
        authKey = KeximAuthKey("TEST_KEY"),
    )

    private companion object {
        val DATE: LocalDate = LocalDate.of(2026, 6, 5)
    }
}
