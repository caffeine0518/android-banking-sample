package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.mock.SeedRecipient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/inquiry/…` 요청 핸들러: 계좌실명조회.
 *
 * (bank_code_std, account_num)으로 수취 디렉터리를 조회해 예금주/상태를 응답하고, 없으면 업무 거절
 * (HTTP 200 + A0001 + bank_rsp_code). 라우팅은 [KftcMockDispatcher], 응답 조립은 [responses], 본문 파싱은 [json].
 */
internal class InquiryRequestHandler(
    private val recipients: List<SeedRecipient>,
    private val responses: KftcMockResponses,
    private val json: Json,
) {
    fun realName(body: String): MockResponse {
        val request = parse(body) ?: return responses.error(MockError.MissingInquiryBody)
        // 계좌번호는 숫자만 정규화해 비교한다. 실제 KFTC도 account_num을 하이픈 없는 정규 숫자로
        // 주고받으며(하이픈은 표시용), 앱 입력도 숫자만 받으므로 시드의 하이픈 표기와 매칭된다.
        val requestDigits = request.accountNum.digitsOnly()
        val match = recipients.firstOrNull {
            it.accountNum.digitsOnly() == requestDigits && it.bankCodeStd == request.bankCodeStd
        } ?: return responses.realNameNotFound(request.accountNum)
        return responses.realNameFound(match)
    }

    private fun parse(body: String): RealNameInquiryRequest? =
        runCatching { json.decodeFromString<RealNameInquiryRequest>(body) }.getOrNull()

    private fun String.digitsOnly(): String = filter(Char::isDigit)
}
