package com.study.bank.data.remote.kftc.mock.http.response

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

// [ok]이 inline이라 private일 수 없다.
internal val MockJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

/** DTO → HTTP 200 JSON. 업무 거절(rsp_code A0001)도 KFTC대로 200이라 같은 함수를 쓴다. */
internal inline fun <reified T> T.ok(): MockResponse =
    jsonResponse(HTTP_OK, MockJson.encodeToString(this))

internal fun jsonResponse(code: Int, body: String): MockResponse = MockResponse()
    .setResponseCode(code)
    .setHeader("Content-Type", "application/json; charset=utf-8")
    .setBody(body)
