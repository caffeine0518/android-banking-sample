package com.study.bank.data.remote.kftc.mock.service.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [KftcWithdrawalService.withdraw] 결과.
 *
 * dispatcher가 전송 오류(4xx)와 업무 거절(HTTP 200 + KFTC rsp_code A0001)로 분기한다:
 * [UnknownSender]/[InvalidAmount]는 입력 오류(4xx), [InsufficientFunds]/[CurrencyMismatch]는
 * KFTC가 200 + 업무 응답코드로 알리는 업무 거절.
 */
internal sealed interface WithdrawResult {

    /**
     * 체결된 출금이체. 그대로 `mock_settled_withdrawals` 한 행이 된다 — 같은 [bankTranId]로 재요청이 오면
     * 원장을 다시 변경하지 않고 이 행을 조회해 같은 응답을 반환한다(이중출금 차단).
     */
    @Entity(tableName = "mock_settled_withdrawals")
    data class Success(
        @PrimaryKey
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

    data class UnknownSender(val fintechUseNum: String) : WithdrawResult

    data class InvalidAmount(val raw: String) : WithdrawResult

    data class InsufficientFunds(val balance: String, val attempted: String) : WithdrawResult

    data class CurrencyMismatch(val from: String, val to: String) : WithdrawResult
}
