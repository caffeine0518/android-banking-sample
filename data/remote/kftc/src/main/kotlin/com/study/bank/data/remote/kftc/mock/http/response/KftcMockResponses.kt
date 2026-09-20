package com.study.bank.data.remote.kftc.mock.http.response

import com.study.bank.data.remote.kftc.api.BANK_RSP_RECIPIENT_NOT_FOUND
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.http.HTTP_OK
import com.study.bank.data.remote.kftc.mock.http.KftcMockDispatcher
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.mapper.toBalanceResponse
import com.study.bank.data.remote.kftc.mock.mapper.toListResponse
import com.study.bank.data.remote.kftc.mock.mapper.toRealNameResponse
import com.study.bank.data.remote.kftc.mock.mapper.toResponse
import com.study.bank.data.remote.kftc.mock.mapper.toTransactionListResponse
import com.study.bank.data.remote.kftc.mock.model.ErrorEnvelope
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.seed.SeedRecipient
import com.study.bank.data.remote.kftc.mock.service.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.TransactionRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

private val MockJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

// 사용자 일련번호. 실서비스에선 OAuth 토큰에서 유도되지만 mock은 고정값을 응답에 넣는다.
private const val USER_SEQ_NO = "1100000001"

private val DTM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * KFTC mock 응답 빌더.
 *
 * 응답 한 건마다 [KftcEnvelope]를 발급해 매퍼에 넘기고, JSON 직렬화 + [MockResponse] 조립까지 담당한다.
 * 라우팅과는 무관하다 — 라우팅은 [KftcMockDispatcher] 책임.
 */
internal class KftcMockResponses {
    private val apiTranSeq = AtomicLong(0)
    private val bankTranSeq = AtomicLong(0)

    fun listFinuse(seed: List<SeedAccount>): MockResponse =
        success(seed.toListResponse(envelope(), USER_SEQ_NO))

    fun balanceFinNum(account: SeedAccount): MockResponse =
        success(account.toBalanceResponse(envelope()))

    /**
     * 거래내역 한 페이지. [hasNext]를 next_page_yn("Y"/"N")으로, [nextCursor]를 befor_inquiry_trace_info로 실어
     * 클라(RemoteMediator)가 연속조회를 이어가게 한다. 마지막 페이지면 hasNext=false·nextCursor="".
     */
    fun transactionList(
        account: SeedAccount,
        records: List<TransactionRecord>,
        hasNext: Boolean,
        nextCursor: String,
    ): MockResponse =
        success(
            records.toTransactionListResponse(
                envelope = envelope(),
                account = account,
                nextPageYn = if (hasNext) "Y" else "N",
                beforInquiryTraceInfo = nextCursor,
            ),
        )

    /** bank_tran_id는 새로 만들지 않고 요청값을 반환한다 — 재요청이 같은 거래여야 한다. */
    fun withdrawSuccess(result: WithdrawResult.Success): MockResponse =
        success(result.toResponse(envelope(bankTranId = result.bankTranId)))

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
        success(recipient.toRealNameResponse(envelope()))

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
        MockJson.encodeToString(
            ErrorEnvelope(
                apiTranId = newApiTranId(),
                apiTranDtm = nowDtm(),
                rspCode = RSP_ERROR,
                rspMessage = error.message,
            ),
        ),
    )

    /** 응답 한 건의 추적 필드. [bankTranId]를 주면 새로 만들지 않는다(출금 재요청은 같은 거래). */
    private fun envelope(bankTranId: String = newBankTranId()) = KftcEnvelope(
        apiTranId = newApiTranId(),
        apiTranDtm = nowDtm(),
        bankTranId = bankTranId,
        bankTranDate = nowDate(),
    )

    private inline fun <reified T> success(body: T): MockResponse =
        jsonResponse(HTTP_OK, MockJson.encodeToString(body))

    private fun jsonResponse(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(body)

    private fun newApiTranId(): String = "T%016d".format(apiTranSeq.incrementAndGet())
    private fun newBankTranId(): String = "M202300001U%06d".format(bankTranSeq.incrementAndGet())
    private fun nowDtm(): String = LocalDateTime.now().format(DTM_FORMATTER)
    private fun nowDate(): String = LocalDateTime.now().format(DATE_FORMATTER)
}
