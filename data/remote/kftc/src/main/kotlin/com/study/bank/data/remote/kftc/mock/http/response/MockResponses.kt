package com.study.bank.data.remote.kftc.mock.http.response

import com.study.bank.data.remote.kftc.network.KftcJson
import kotlinx.serialization.encodeToString
import okhttp3.mockwebserver.MockResponse

/** DTO를 HTTP 200 JSON 응답으로 변환한다. KFTC는 업무 거절(rsp_code A0001)도 200으로 응답하므로 같은 함수를 쓴다. */
internal inline fun <reified T> T.ok(): MockResponse =
    jsonResponse(HTTP_OK, KftcJson.encodeToString(this))

internal fun jsonResponse(code: Int, body: String): MockResponse = MockResponse()
    .setResponseCode(code)
    .setHeader("Content-Type", "application/json; charset=utf-8")
    .setBody(body)
