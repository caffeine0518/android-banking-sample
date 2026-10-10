package com.study.bank.data.remote.kftc.mock.service.model

import androidx.room.ColumnInfo
import com.study.bank.data.remote.kftc.mock.storage.entity.SettledWithdrawal

/**
 * [IdempotencyConflict]·[UnknownSender]·[InvalidAmount]는 4xx로, [InsufficientFunds]·[CurrencyMismatch]는
 * HTTP 200 업무 거절로 응답한다.
 */
internal sealed interface WithdrawResult {

    /** 같은 [bankTranId]의 재요청에 그대로 반환하도록 [SettledWithdrawal]에 저장한다. */
    data class Success(
        @ColumnInfo(name = "bank_tran_id")
        val bankTranId: String,
        @ColumnInfo(name = "fintech_use_num")
        val fintechUseNum: String,
        @ColumnInfo(name = "bank_code_std")
        val bankCodeStd: String,
        @ColumnInfo(name = "account_num_masked")
        val accountNumMasked: String,
        @ColumnInfo(name = "account_holder_name")
        val accountHolderName: String,
        @ColumnInfo(name = "tran_amt")
        val tranAmt: String,
        @ColumnInfo(name = "after_balance_amt")
        val afterBalanceAmt: String,
    ) : WithdrawResult

    data class IdempotencyConflict(val bankTranId: String) : WithdrawResult

    data class UnknownSender(val fintechUseNum: String) : WithdrawResult

    data class InvalidAmount(val raw: String) : WithdrawResult

    data class InsufficientFunds(val balance: String, val attempted: String) : WithdrawResult

    data class CurrencyMismatch(val from: String, val to: String) : WithdrawResult
}
