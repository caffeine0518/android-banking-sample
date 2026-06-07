package com.study.bank.data.remote.fx.network

import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit

@Singleton
class KeximRetrofit @Inject constructor(
    okHttp: KeximOkHttpClient,
    json: KeximJson,
) {
    val value: Retrofit = Retrofit.Builder()
        .baseUrl(KEXIM_BASE_URL)
        .client(okHttp.value)
        .addConverterFactory(json.converterFactory)
        .build()

    private companion object {
        const val KEXIM_BASE_URL = "https://oapi.koreaexim.go.kr/"
    }
}
