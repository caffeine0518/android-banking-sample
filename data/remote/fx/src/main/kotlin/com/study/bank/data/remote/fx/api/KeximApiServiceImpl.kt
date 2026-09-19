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

    override suspend fun getRates(date: LocalDate): List<KeximRateItem> =
        httpApi.getRates(
            authKey = authKey.value,
            searchDate = date.format(DATE_FMT),
        )

    private companion object {
        private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }
}
