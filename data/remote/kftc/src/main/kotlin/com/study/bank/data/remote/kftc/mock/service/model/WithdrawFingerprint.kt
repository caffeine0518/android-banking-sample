package com.study.bank.data.remote.kftc.mock.service.model

import androidx.room.ColumnInfo

/** 같은 bank_tran_id의 재요청이 같은 거래인지 판정한다. 멱등 재시도는 같은 요청이므로 정규화하지 않는다. */
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
