package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [seq]는 KFTC 거래내역에 없는 행 id를 mock이 부여한 것으로, 연속조회 커서와 합성 TransactionId의 키로 쓴다.
 * [seeded]는 앱 시작 시 적재한 과거 거래와 세션 이체를 구분한다.
 */
@Entity(
    tableName = "mock_transactions",
    indices = [Index(value = ["fintech_use_num", "seq"])],
)
internal data class TransactionRecord(
    @ColumnInfo(name = "fintech_use_num")
    val fintechUseNum: String,
    @ColumnInfo(name = "tran_date")
    val tranDate: String,
    @ColumnInfo(name = "tran_time")
    val tranTime: String,
    @ColumnInfo(name = "direction")
    val direction: TransactionDirection,
    @ColumnInfo(name = "print_content")
    val printContent: String,
    @ColumnInfo(name = "tran_amt")
    val tranAmt: String,
    @ColumnInfo(name = "after_balance_amt")
    val afterBalanceAmt: String,
    @ColumnInfo(name = "counterparty_name")
    val counterpartyName: String?,
    @ColumnInfo(name = "seeded")
    val seeded: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "seq")
    val seq: Long = 0,
)
