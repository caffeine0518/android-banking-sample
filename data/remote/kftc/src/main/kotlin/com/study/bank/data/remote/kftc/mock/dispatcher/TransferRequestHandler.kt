package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcBankState
import com.study.bank.data.remote.kftc.mock.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.WithdrawResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import java.util.concurrent.TimeUnit

/**
 * KFTC `/v2.0/transfer/…` 요청 핸들러: 출금이체.
 *
 * 본문 역직렬화 → [state] 위임 → 결과를 전송 오류(4xx)/업무 거절(200+A0001)/성공으로 응답 분기한다.
 * 라우팅은 [KftcMockDispatcher], 응답 조립은 [responses], 본문 파싱은 [json]에 위임.
 *
 * [responseDelayMillis]>0이면 출금 응답을 그만큼 지연시킨다 — 데모/수동 테스트에서 "보내는 중이에요"
 * 로딩 화면을 눈으로 확인할 수 있게 한다. 기본 0(지연 없음)이라 단위 테스트는 느려지지 않고, 실제 앱
 * wiring([com.study.bank.data.remote.kftc.mock.KftcMockServer])에서만 양수를 준다.
 */
internal class TransferRequestHandler(
    private val state: KftcBankState,
    private val responses: KftcMockResponses,
    private val json: Json,
    private val responseDelayMillis: Long = 0,
) {
    fun withdraw(body: String): MockResponse {
        val command = parseWithdraw(body) ?: return responses.error(MockError.MissingTransferBody)
        val response = when (val result = state.withdraw(command)) {
            is WithdrawResult.Success -> responses.withdrawSuccess(result)
            is WithdrawResult.UnknownSender ->
                responses.error(MockError.UnknownFintechUseNum(result.fintechUseNum))
            is WithdrawResult.InvalidAmount -> responses.error(MockError.InvalidTranAmt(result.raw))
            is WithdrawResult.InsufficientFunds ->
                responses.withdrawFailure(BANK_RSP_INSUFFICIENT_FUNDS, "출금계좌 잔액 부족")
            is WithdrawResult.CurrencyMismatch ->
                responses.withdrawFailure(BANK_RSP_CURRENCY_MISMATCH, "통화 불일치: ${result.from}→${result.to}")
        }
        return if (responseDelayMillis > 0) {
            response.setBodyDelay(responseDelayMillis, TimeUnit.MILLISECONDS)
        } else {
            response
        }
    }

    private fun parseWithdraw(body: String): WithdrawCommand? = runCatching {
        val dto = json.decodeFromString<WithdrawTransferRequest>(body)
        WithdrawCommand(
            fintechUseNum = dto.fintechUseNum,
            tranAmt = dto.tranAmt,
            recvAccountNum = dto.recvClientAccountNum,
            recvBankCode = dto.recvClientBankCodeStd,
            recvName = dto.recvClientName,
            reqName = dto.reqClientName,
            wdPrintContent = dto.wdPrintContent,
            dpsPrintContent = dto.dpsPrintContent,
        )
    }.getOrNull()
}
