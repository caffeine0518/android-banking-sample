package com.study.bank.data.remote.fx.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.study.bank.data.remote.fx.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

internal const val KEXIM_PROD_BASE_URL = "https://oapi.koreaexim.go.kr/"

/**
 * 테스트용 [KeximApiService] 빌더. 실제 앱에선 DI 프레임워크가 처리할 의존성 묶음을 수동으로 구성.
 *
 * 라이브 테스트는 디폴트(KEXIM 운영 baseUrl + BuildConfig 인증키),
 * 미래에 MockWebServer 기반 테스트가 추가되면 [baseUrl]에 `mockServer.url("/")` 주입.
 * 인증 실패 시나리오는 [authKey]에 임의 문자열 주입.
 */
internal fun createKeximApiService(
    baseUrl: String = KEXIM_PROD_BASE_URL,
    authKey: String = BuildConfig.KEXIM_API_KEY,
    json: Json = DefaultTestJson,
): KeximApiService = KeximApiServiceImpl(
    httpApi = createKeximHttpApi(baseUrl, json),
    authKey = authKey,
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
