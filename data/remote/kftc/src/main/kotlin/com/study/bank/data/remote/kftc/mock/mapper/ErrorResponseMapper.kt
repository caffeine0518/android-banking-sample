package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.http.response.MockError
import com.study.bank.data.remote.kftc.mock.http.response.jsonResponse
import com.study.bank.data.remote.kftc.mock.model.ErrorEnvelope
import com.study.bank.data.remote.kftc.network.KftcJson
import okhttp3.mockwebserver.MockResponse

/**
 * [MockError]를 오류 응답으로 변환한다.
 *
 * 다른 매퍼와 달리 DTO가 아니라 [MockResponse]를 반환한다. HTTP 상태 코드가 [MockError]에 포함돼 있어
 * 여기서 함께 설정하는 편이 호출부에서 다시 꺼내 쓰는 것보다 짧다.
 */
internal class ErrorResponseMapper(private val tranIds: KftcTranIds) {

    fun toResponse(error: MockError): MockResponse {
        val envelope = ErrorEnvelope(
            apiTranId = tranIds.newApiTranId(),
            apiTranDtm = tranIds.nowDtm(),
            rspCode = RSP_ERROR,
            rspMessage = error.message,
        )
        // 생성된 serializer를 직접 전달한다. reified 버전을 쓰면 IDE가 InternalSerializationApi opt-in 경고를
        // 표시한다(컴파일러는 경고하지 않는다).
        return jsonResponse(
            error.httpCode,
            KftcJson.encodeToString(ErrorEnvelope.serializer(), envelope),
        )
    }
}
