package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.mock.http.response.MockError
import com.study.bank.data.remote.kftc.mock.http.response.ok
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import com.study.bank.data.remote.kftc.mock.mapper.InquiryResponseMapper
import com.study.bank.data.remote.kftc.mock.model.SeedRecipient
import com.study.bank.data.remote.kftc.mock.model.sameDigitsAs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

internal class InquiryRequestHandler(
    private val recipients: List<SeedRecipient>,
    private val mapper: InquiryResponseMapper,
    private val errors: ErrorResponseMapper,
    private val json: Json,
) {
    fun realName(body: String): MockResponse {
        val request = parse(body) ?: return errors.toResponse(MockError.MissingInquiryBody)
        return when (val match = recipients.firstOrNull { it.matches(request) }) {
            null -> mapper.toNotFoundResponse(request.accountNum)
            else -> mapper.toResponse(match)
        }.ok()
    }

    private fun SeedRecipient.matches(request: RealNameInquiryRequest): Boolean =
        bankCodeStd == request.bankCodeStd && accountNum sameDigitsAs request.accountNum

    private fun parse(body: String): RealNameInquiryRequest? =
        runCatching { json.decodeFromString<RealNameInquiryRequest>(body) }.getOrNull()
}
