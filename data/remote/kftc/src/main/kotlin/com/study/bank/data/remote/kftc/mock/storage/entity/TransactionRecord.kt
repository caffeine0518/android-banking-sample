package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mock 은행 거래원장 한 행(`mock_transactions`). 금액·잔액 문자열은 통화별 소수 자릿수를 유지한다.
 *
 * [seq]는 SQLite가 부여하는 단조 증가 PK(클수록 최신)다. KFTC 거래내역에는 행 id가 없어 mock이 부여한다.
 * 연속조회 커서의 유일한 정렬 기준(같은 초의 거래도 누락 없이 조회)이고, 합성 TransactionId가 충돌하지 않는 근거다.
 * 시드 거래내역을 **오래된 순으로** 먼저 적재하므로 seq 순서와 시간 순서가 일치하고, 세션 이체는 그 뒤에
 * 삽입돼 자동으로 더 큰 seq를 받는다.
 *
 * [seeded]는 앱 시작 시 적재한 과거 거래(true)와 이번 세션의 이체(false)를 구분한다.
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
