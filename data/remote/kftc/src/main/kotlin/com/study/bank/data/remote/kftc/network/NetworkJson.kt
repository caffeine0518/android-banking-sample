package com.study.bank.data.remote.kftc.network

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter

/**
 * KFTC 모듈 공용 Json 설정. 클라이언트 직렬화와 mock 응답 인코딩이 같은 인스턴스를 쓴다.
 *
 * `encodeDefaults`가 필요한 이유: KFTC는 `transfer_purpose`처럼 DTO에 기본값이 있는 필드도 필수로 요구한다.
 * 이 설정이 없으면 기본값인 필드가 요청 body에서 제외되는데, mock은 디코더 기본값을 적용해 200을 반환하므로
 * 테스트가 누락을 검출하지 못한다.
 */
internal val KftcJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

@Singleton
class NetworkJson @Inject constructor() {

    val value: Json = KftcJson

    val converterFactory: Converter.Factory =
        value.asConverterFactory("application/json".toMediaType())
}
