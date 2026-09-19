package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.BANK_RSP_CURRENCY_MISMATCH
import com.study.bank.data.remote.kftc.mock.http.response.BANK_RSP_INSUFFICIENT_FUNDS
import com.study.bank.data.remote.kftc.mock.http.response.KftcMockResponses
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.service.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.WithdrawResult
import java.util.concurrent.TimeUnit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/transfer/…` 요청 핸들러: 출금이체.
 *
 * [responseDelayMillis]>0이면 출금 응답을 그만큼 지연시킨다 — 데모/수동 테스트에서 "보내는 중이에요"
 * 로딩 화면을 눈으로 확인하려는 용도다. 기본 0이라 단위 테스트는 느려지지 않고, 실제 앱
 * wiring([com.study.bank.data.remote.kftc.mock.KftcMockServer])에서만 양수를 준다.
 */
internal class TransferRequestHandler(
    private val withdrawalService: KftcWithdrawalService,
    private val responses: KftcMockResponses,
    private val json: Json,
    private val responseDelayMillis: Long = 0,
) {
    fun withdraw(body: String): MockResponse {
        val command = parseWithdraw(body) ?: return responses.error(MockError.MissingTransferBody)
        val response = when (val result = withdrawalService.withdraw(command)) {
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
            bankTranId = dto.bankTranId,
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
