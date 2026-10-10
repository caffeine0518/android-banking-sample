package com.study.bank.data.remote.kftc.mock.http.response

import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper

/**
 * Mock 서버의 오류 응답 종류.
 *
 * 메시지와 HTTP 상태 코드를 호출 측에 하드코딩하지 않도록 여기에 모은다. 응답 생성은 [ErrorResponseMapper]가
 * 담당하고 rsp_code는 모두 [RSP_ERROR]다.
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

    /** 경로는 있지만 HTTP 메서드가 다른 경우. 예: 조회 경로에 POST, 이체 경로에 GET. */
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

    /** 같은 bank_tran_id가 내용이 다른 거래에 재사용된 경우. 클라이언트의 키 발급 결함이므로 4xx로 응답한다. */
    data class DuplicateBankTranId(val bankTranId: String) : MockError {
        override val httpCode = HTTP_CONFLICT
        override val message = "다른 거래에 이미 사용된 bank_tran_id: $bankTranId"
    }

    data object MissingInquiryBody : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "계좌실명조회 요청 본문 누락/파싱 실패"
    }
}
