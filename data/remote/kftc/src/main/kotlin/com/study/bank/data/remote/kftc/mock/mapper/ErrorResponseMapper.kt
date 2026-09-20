package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.http.response.jsonResponse
import com.study.bank.data.remote.kftc.mock.http.response.toJson
import com.study.bank.data.remote.kftc.mock.model.ErrorEnvelope
import okhttp3.mockwebserver.MockResponse

/**
 * [MockError] → 에러 응답.
 *
 * 다른 매퍼와 달리 DTO가 아니라 [MockResponse]를 반환한다 — HTTP 상태코드가 [MockError]의 일부라
 * 여기서 함께 붙이는 편이 호출부에서 다시 꺼내 쓰는 것보다 짧다.
 */
internal class ErrorResponseMapper(private val tranIds: KftcTranIds) {

    fun toResponse(error: MockError): MockResponse = jsonResponse(
        error.httpCode,
        ErrorEnvelope(
            apiTranId = tranIds.newApiTranId(),
            apiTranDtm = tranIds.nowDtm(),
            rspCode = RSP_ERROR,
            rspMessage = error.message,
        ).toJson(),
    )
}
