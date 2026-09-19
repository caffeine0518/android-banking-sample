package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 한국수출입은행(KEXIM) 환율조회 Retrofit 바인딩.
 *
 * baseUrl `https://oapi.koreaexim.go.kr/` · 한도 인증키별 일 1,000회.
 * authkey는 query 강제(KEXIM 스펙) — OkHttp 로깅에 redactQueryParams("authkey") 필수.
 *
 * 직접 호출하지 말 것 — authkey/날짜 포맷팅을 가린 [KeximApiService]가 정상 경로다(public은 DI 때문).
 */
interface KeximHttpApi {

    @GET("site/program/financial/exchangeJSON")
    suspend fun getRates(
        @Query("authkey") authKey: String,
        @Query("searchdate") searchDate: String,
        @Query("data") dataType: String = "AP01",
    ): List<KeximRateItem>
}
