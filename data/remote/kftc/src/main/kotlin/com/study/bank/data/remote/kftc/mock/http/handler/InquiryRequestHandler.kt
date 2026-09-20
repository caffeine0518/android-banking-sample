package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.ok
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import com.study.bank.data.remote.kftc.mock.mapper.InquiryResponseMapper
import com.study.bank.data.remote.kftc.mock.seed.SeedRecipient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/inquiry/…` 계좌실명조회 핸들러.
 */
internal class InquiryRequestHandler(
    private val recipients: List<SeedRecipient>,
    private val mapper: InquiryResponseMapper,
    private val errors: ErrorResponseMapper,
    private val json: Json,
) {
    fun realName(body: String): MockResponse {
        val request = parse(body) ?: return errors.toResponse(MockError.MissingInquiryBody)
        val requestDigits = request.accountNum.digitsOnly()
        val match = recipients.firstOrNull {
            it.accountNum.digitsOnly() == requestDigits && it.bankCodeStd == request.bankCodeStd
        } ?: return mapper.toNotFoundResponse(request.accountNum).ok()
        return mapper.toResponse(match).ok()
    }

    private fun parse(body: String): RealNameInquiryRequest? =
        runCatching { json.decodeFromString<RealNameInquiryRequest>(body) }.getOrNull()

    /**
     * 계좌번호 비교용 정규화. 실제 KFTC는 account_num을 하이픈 없는 숫자로 주고받고(하이픈은 표시용)
     * 시드는 하이픈 표기를 쓰므로, 양쪽에서 숫자만 남겨 비교한다.
     */
    private fun String.digitsOnly(): String = filter(Char::isDigit)
}
