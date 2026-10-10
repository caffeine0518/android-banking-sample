package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeximApiServiceImpl @Inject constructor(
    private val httpApi: KeximHttpApi,
    private val authKey: KeximAuthKey,
) : KeximApiService {

    override suspend fun getRates(date: LocalDate): KeximRates =
        httpApi.getRates(
            authKey = authKey.value,
            searchDate = date.format(DATE_FMT),
        ).toKeximRates()

    // 빈 배열과 result 2(비영업일)는 모두 NotPublished다.
    private fun List<KeximRateItem>.toKeximRates(): KeximRates = when (firstOrNull()?.result) {
        RESULT_SUCCESS -> KeximRates.Published(filter { it.result == RESULT_SUCCESS })
        RESULT_INVALID_KEY -> KeximRates.InvalidKey
        RESULT_LIMIT_EXCEEDED -> KeximRates.LimitExceeded
        else -> KeximRates.NotPublished
    }

    private companion object {
        private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        const val RESULT_SUCCESS = 1
        const val RESULT_INVALID_KEY = 3
        const val RESULT_LIMIT_EXCEEDED = 4
    }
}
