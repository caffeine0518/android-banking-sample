package com.study.bank.data.remote.kftc.mock

/**
 * [KftcBankState.withdraw] 결과.
 *
 * dispatcher가 전송 오류(4xx)와 업무 거절(HTTP 200 + KFTC rsp_code A0001)로 분기한다:
 * [UnknownSender]/[InvalidAmount]는 입력 오류(4xx), [InsufficientFunds]/[CurrencyMismatch]는
 * KFTC가 200 + 업무 응답코드로 알리는 업무 거절.
 */
internal sealed interface WithdrawResult {

    data class Success(
        val fintechUseNum: String,
        val bankCodeStd: String,
        val accountNumMasked: String,
        val accountHolderName: String,
        val tranAmt: String,
        val afterBalanceAmt: String,
    ) : WithdrawResult

    data class UnknownSender(val fintechUseNum: String) : WithdrawResult

    data class InvalidAmount(val raw: String) : WithdrawResult

    data class InsufficientFunds(val balance: String, val attempted: String) : WithdrawResult

    data class CurrencyMismatch(val from: String, val to: String) : WithdrawResult
}
