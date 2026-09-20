package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.api.BANK_RSP_CURRENCY_MISMATCH
import com.study.bank.data.remote.kftc.api.BANK_RSP_INSUFFICIENT_FUNDS
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcMockServerImpl
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.KftcEnvelopes
import com.study.bank.data.remote.kftc.mock.http.response.ok
import com.study.bank.data.remote.kftc.mock.http.response.toResponse
import com.study.bank.data.remote.kftc.mock.mapper.toResponse
import com.study.bank.data.remote.kftc.mock.mapper.withdrawRejected
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
    private val envelopes: KftcEnvelopes,
    private val json: Json,
    private val responseDelayMillis: Long = 0,
) {
    fun withdraw(body: String): MockResponse {
        val command = parseWithdraw(body)
            ?: return MockError.MissingTransferBody.toResponse(envelopes.next())
        val response = withdrawalService.withdraw(command).toMockResponse()
        return if (responseDelayMillis > 0) {
            response.setBodyDelay(responseDelayMillis, TimeUnit.MILLISECONDS)
        } else {
            response
        }
    }

    private fun WithdrawResult.toMockResponse(): MockResponse = when (this) {
        is WithdrawResult.Success -> {
            // bank_tran_id는 새로 만들지 않고 요청값을 그대로 쓴다 — 재요청이 같은 거래여야 한다.
            toResponse(envelopes.next(bankTranId = bankTranId)).ok()
        }

        is WithdrawResult.UnknownSender -> {
            MockError.UnknownFintechUseNum(fintechUseNum).toResponse(envelopes.next())
        }

        is WithdrawResult.InvalidAmount -> {
            MockError.InvalidTranAmt(raw).toResponse(envelopes.next())
        }

        is WithdrawResult.InsufficientFunds -> {
            withdrawRejected(envelopes.next(), BANK_RSP_INSUFFICIENT_FUNDS, "출금계좌 잔액 부족").ok()
        }

        is WithdrawResult.CurrencyMismatch -> {
            withdrawRejected(envelopes.next(), BANK_RSP_CURRENCY_MISMATCH, "통화 불일치: $from→$to").ok()
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
