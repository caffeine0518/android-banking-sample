package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.KftcEnvelopes
import com.study.bank.data.remote.kftc.mock.http.response.ok
import com.study.bank.data.remote.kftc.mock.http.response.toResponse
import com.study.bank.data.remote.kftc.mock.mapper.realNameNotFound
import com.study.bank.data.remote.kftc.mock.mapper.toRealNameResponse
import com.study.bank.data.remote.kftc.mock.seed.SeedRecipient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/inquiry/…` 계좌실명조회 핸들러.
 */
internal class InquiryRequestHandler(
    private val recipients: List<SeedRecipient>,
    private val envelopes: KftcEnvelopes,
    private val json: Json,
) {
    fun realName(body: String): MockResponse {
        val request = parse(body)
            ?: return MockError.MissingInquiryBody.toResponse(envelopes.next())
        val requestDigits = request.accountNum.digitsOnly()
        val match = recipients.firstOrNull {
            it.accountNum.digitsOnly() == requestDigits && it.bankCodeStd == request.bankCodeStd
        } ?: return realNameNotFound(envelopes.next(), request.accountNum).ok()
        return match.toRealNameResponse(envelopes.next()).ok()
    }

    private fun parse(body: String): RealNameInquiryRequest? =
        runCatching { json.decodeFromString<RealNameInquiryRequest>(body) }.getOrNull()

    /**
     * 계좌번호 비교용 정규화. 실제 KFTC는 account_num을 하이픈 없는 숫자로 주고받고(하이픈은 표시용)
     * 시드는 하이픈 표기를 쓰므로, 양쪽에서 숫자만 남겨 비교한다.
     */
    private fun String.digitsOnly(): String = filter(Char::isDigit)
}
