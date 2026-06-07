package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [KeximApiService] 구현체.
 *
 * 책임:
 *  - 호출마다 [authKey] 자동 주입
 *  - [LocalDate] → KEXIM `yyyyMMdd` 포맷 변환
 *  - 그 외 HTTP 디테일은 [httpApi]로 위임
 */
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
