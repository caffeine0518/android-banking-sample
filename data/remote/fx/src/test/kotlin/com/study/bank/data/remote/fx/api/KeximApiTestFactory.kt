package com.study.bank.data.remote.fx.api

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.study.bank.data.remote.fx.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

internal const val KEXIM_PROD_BASE_URL = "https://oapi.koreaexim.go.kr/"

internal fun createKeximApiService(
    baseUrl: String = KEXIM_PROD_BASE_URL,
    authKey: String = BuildConfig.KEXIM_API_KEY,
    json: Json = DefaultTestJson,
): KeximApiService = KeximApiServiceImpl(
    httpApi = createKeximHttpApi(baseUrl, json),
    authKey = KeximAuthKey(authKey),
)

private fun createKeximHttpApi(baseUrl: String, json: Json): KeximHttpApi =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(KeximHttpApi::class.java)

private val DefaultTestJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
