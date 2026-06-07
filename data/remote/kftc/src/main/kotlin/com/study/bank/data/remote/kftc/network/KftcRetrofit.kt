package com.study.bank.data.remote.kftc.network

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit

@Singleton
class KftcRetrofit @Inject constructor(
    mockServer: KftcMockServer,
    okHttp: NetworkOkHttpClient,
    json: NetworkJson,
) {
    val value: Retrofit = Retrofit.Builder()
        .baseUrl(mockServer.baseUrl().toString())
        .client(okHttp.value)
        .addConverterFactory(json.converterFactory)
        .build()
}
