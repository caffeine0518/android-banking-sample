package com.study.bank.data.remote.kftc.mock.service.model

import androidx.room.ColumnInfo

/**
 * 같은 bank_tran_id 의 재요청이 체결된 거래와 같은 거래인지 판정하는 요청 지문. data class 동등성으로 비교한다.
 *
 * 값은 요청 원문 그대로다 — 멱등 재시도는 같은 요청을 다시 보내는 것이므로 정규화하지 않는다.
 */
internal data class WithdrawFingerprint(
    @ColumnInfo(name = "fintech_use_num")
    val fintechUseNum: String,
    @ColumnInfo(name = "recv_bank_code")
    val recvBankCode: String,
    @ColumnInfo(name = "recv_account_num")
    val recvAccountNum: String,
    @ColumnInfo(name = "tran_amt")
    val tranAmt: String,
)
