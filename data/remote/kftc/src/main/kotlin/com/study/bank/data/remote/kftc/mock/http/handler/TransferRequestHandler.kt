package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcMockServerImpl
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
 * KFTC `/v2.0/transfer/…` 출금이체 핸들러.
 *
 * [responseDelayMillis] 기본값 0 — 테스트는 지연 없이 실행되고, 데모용 지연은
 * [KftcMockServerImpl]에서만 주입한다.
 */
internal class TransferRequestHandler(
    private val withdrawalService: KftcWithdrawalService,
    private val responses: KftcMockResponses,
    private val json: Json,
    private val responseDelayMillis: Long = 0,
) {
    fun withdraw(body: String): MockResponse {
        val command = parseWithdraw(body) ?: return responses.error(MockError.MissingTransferBody)
        val response = withdrawalService.withdraw(command).toResponse()
        return if (responseDelayMillis > 0) {
            response.setBodyDelay(responseDelayMillis, TimeUnit.MILLISECONDS)
        } else {
            response
        }
    }

    private fun WithdrawResult.toResponse(): MockResponse = when (this) {
        is WithdrawResult.Success -> {
            responses.withdrawSuccess(this)
        }

        is WithdrawResult.UnknownSender -> {
            responses.error(MockError.UnknownFintechUseNum(fintechUseNum))
        }

        is WithdrawResult.InvalidAmount -> {
            responses.error(MockError.InvalidTranAmt(raw))
        }

        is WithdrawResult.InsufficientFunds -> {
            responses.withdrawFailure(BANK_RSP_INSUFFICIENT_FUNDS, "출금계좌 잔액 부족")
        }

        is WithdrawResult.CurrencyMismatch -> {
            responses.withdrawFailure(BANK_RSP_CURRENCY_MISMATCH, "통화 불일치: $from→$to")
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
