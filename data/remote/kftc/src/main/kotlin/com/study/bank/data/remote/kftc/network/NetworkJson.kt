package com.study.bank.data.remote.kftc.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter

internal val KftcJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    // `transfer_purpose`처럼 기본값이 있는 필드도 KFTC 필수 필드다. 빠지면 mock이 디코더 기본값으로 처리해
    // 200을 반환하므로 테스트가 누락을 검출하지 못한다.
    encodeDefaults = true
}

@Singleton
class NetworkJson @Inject constructor() {

    val value: Json = KftcJson

    val converterFactory: Converter.Factory =
        value.asConverterFactory("application/json".toMediaType())
}
