package com.study.bank.data.remote.kftc.mock.http

import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.mock.http.response.toResponse

/**
 * Mock 디스패처가 만들어내는 에러 케이스.
 *
 * 메시지/HTTP 상태코드를 호출 측에서 하드코딩하지 않도록 격리. 케이스 이름이 곧 의도가 되어
 * [KftcMockDispatcher]가 `MockError.UnknownEndpoint(path).toResponse(…)` 같은 문장으로 읽힘.
 * 응답 조립은 [toResponse]가 맡고 rsp_code는 모두 [RSP_ERROR]로 통일한다.
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

    /** 경로는 있으나 HTTP 메서드가 다른 경우 — 조회 경로에 POST, 이체 경로에 GET 같은 요청. */
    data class MethodNotAllowed(val method: String, val path: String) : MockError {
        override val httpCode = HTTP_METHOD_NOT_ALLOWED
        override val message = "$path 는 $method 를 지원하지 않는다"
    }

    data object MissingFintechUseNum : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "fintech_use_num 쿼리 누락"
    }

    data class UnknownFintechUseNum(val fintechUseNum: String) : MockError {
        override val httpCode = HTTP_NOT_FOUND
        override val message = "존재하지 않는 fintech_use_num: $fintechUseNum"
    }

    data object MissingTransferBody : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "출금이체 요청 본문 누락/파싱 실패"
    }

    data class InvalidTranAmt(val raw: String) : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "유효하지 않은 tran_amt: $raw"
    }

    data object MissingInquiryBody : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "계좌실명조회 요청 본문 누락/파싱 실패"
    }

}
