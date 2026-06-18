package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.SeedAccount
import com.study.bank.data.remote.kftc.mock.SeedRecipient
import com.study.bank.data.remote.kftc.mock.TransactionRecord
import com.study.bank.data.remote.kftc.mock.WithdrawResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

private val DefaultMockJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

private val DTM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * KFTC mock 응답 빌더.
 *
 * envelope 추적 필드(api_tran_id, api_tran_dtm, bank_tran_id) 채움 + JSON 직렬화 + [MockResponse]
 * 조립까지 담당. 라우팅과는 무관하다 — 라우팅은 [KftcMockDispatcher] 책임.
 */
internal class KftcMockResponses(
    private val json: Json = DefaultMockJson,
    private val clock: () -> LocalDateTime = LocalDateTime::now,
) {
    private val apiTranSeq = AtomicLong(0)
    private val bankTranSeq = AtomicLong(0)

    fun listFinuse(seed: List<SeedAccount>): MockResponse =
        success(seed.toListResponse(newApiTranId(), nowDtm(), USER_SEQ_NO))

    fun balanceFinNum(account: SeedAccount): MockResponse =
        success(account.toBalanceResponse(newApiTranId(), nowDtm(), newBankTranId()))

    fun transactionList(account: SeedAccount, records: List<TransactionRecord>): MockResponse =
        success(records.toTransactionListResponse(account, newApiTranId(), nowDtm(), newBankTranId()))

    fun withdrawSuccess(result: WithdrawResult.Success): MockResponse =
        success(result.toResponse(newApiTranId(), nowDtm(), newBankTranId(), nowDate()))

    /** 업무 거절: KFTC대로 HTTP 200 + rsp_code A0001 + 식별용 bank_rsp_code. 성공과 같은 DTO를 재사용. */
    fun withdrawFailure(bankRspCode: String, message: String): MockResponse =
        success(
            WithdrawTransferResponse(
                apiTranId = newApiTranId(),
                apiTranDtm = nowDtm(),
                rspCode = RSP_ERROR,
                rspMessage = message,
                bankRspCode = bankRspCode,
            ),
        )

    fun realNameFound(recipient: SeedRecipient): MockResponse =
        success(recipient.toRealNameResponse(newApiTranId(), nowDtm(), newBankTranId(), nowDate()))

    /** 수취 계좌 미존재: HTTP 200 + rsp_code A0001 + bank_rsp_code. */
    fun realNameNotFound(accountNum: String): MockResponse =
        success(
            RealNameInquiryResponse(
                apiTranId = newApiTranId(),
                apiTranDtm = nowDtm(),
                rspCode = RSP_ERROR,
                rspMessage = "조회된 예금주가 없습니다",
                bankRspCode = BANK_RSP_RECIPIENT_NOT_FOUND,
                accountNum = accountNum,
            ),
        )

    fun error(error: MockError): MockResponse = jsonResponse(
        error.httpCode,
        json.encodeToString(
            ErrorEnvelope(
                apiTranId = newApiTranId(),
                apiTranDtm = nowDtm(),
                rspCode = RSP_ERROR,
                rspMessage = error.message,
            ),
        ),
    )

    private inline fun <reified T> success(body: T): MockResponse =
        jsonResponse(HTTP_OK, json.encodeToString(body))

    private fun jsonResponse(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(body)

    private fun newApiTranId(): String = "T%016d".format(apiTranSeq.incrementAndGet())
    private fun newBankTranId(): String = "M202300001U%06d".format(bankTranSeq.incrementAndGet())
    private fun nowDtm(): String = clock().format(DTM_FORMATTER)
    private fun nowDate(): String = clock().format(DATE_FORMATTER)

    @Serializable
    private data class ErrorEnvelope(
        @SerialName("api_tran_id") val apiTranId: String,
        @SerialName("api_tran_dtm") val apiTranDtm: String,
        @SerialName("rsp_code") val rspCode: String,
        @SerialName("rsp_message") val rspMessage: String,
    )
}
