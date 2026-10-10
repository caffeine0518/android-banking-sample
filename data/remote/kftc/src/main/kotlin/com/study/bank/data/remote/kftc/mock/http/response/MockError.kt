package com.study.bank.data.remote.kftc.mock.http.response

import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper

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

    /** 클라이언트의 키 발급 결함이므로 4xx로 응답한다. */
    data class DuplicateBankTranId(val bankTranId: String) : MockError {
        override val httpCode = HTTP_CONFLICT
        override val message = "다른 거래에 이미 사용된 bank_tran_id: $bankTranId"
    }

    data object MissingInquiryBody : MockError {
        override val httpCode = HTTP_BAD_REQUEST
        override val message = "계좌실명조회 요청 본문 누락/파싱 실패"
    }
}
