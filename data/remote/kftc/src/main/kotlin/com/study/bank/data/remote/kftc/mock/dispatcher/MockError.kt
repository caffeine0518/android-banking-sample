package com.study.bank.data.remote.kftc.mock.dispatcher

/**
 * Mock 디스패처가 만들어내는 에러 케이스.
 *
 * 메시지/HTTP 상태코드를 호출 측에서 하드코딩하지 않도록 격리. 케이스 이름이 곧 의도가 되어
 * [KftcMockDispatcher]가 `responses.error(MockError.UnknownEndpoint(path))` 같은 문장으로 읽힘.
 * KFTC envelope의 rsp_code는 [KftcMockResponses]가 모두 [RSP_ERROR]로 통일.
 */
internal sealed interface MockError {
    val httpCode: Int
    val message: String

    data object InvalidUrl : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "요청 URL 파싱 실패"
    }

    data class UnknownEndpoint(val path: String) : MockError {
        override val httpCode = HTTP_NOT_FOUND
        override val message = "지원하지 않는 경로: $path"
    }

    data object MissingFintechUseNum : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "fintech_use_num 쿼리 누락"
    }

    data class UnknownFintechUseNum(val fintechUseNum: String) : MockError {
        override val httpCode = HTTP_NOT_FOUND
        override val message = "존재하지 않는 fintech_use_num: $fintechUseNum"
    }
}
