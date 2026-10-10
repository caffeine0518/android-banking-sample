package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import retrofit2.http.GET
import retrofit2.http.Query

/** [KeximApiService]를 거쳐 호출한다. DI 때문에 public이다. */
interface KeximHttpApi {

    @GET("site/program/financial/exchangeJSON")
    suspend fun getRates(
        // KEXIM 스펙상 query로만 받는다. URL이 로그에 남으므로 로깅할 때 가려야 한다.
        @Query("authkey") authKey: String,
        @Query("searchdate") searchDate: String,
        @Query("data") dataType: String = "AP01",
    ): List<KeximRateItem>
}
