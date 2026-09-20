package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mock 은행 거래원장 한 줄(`mock_transactions`). 금액/잔액 문자열은 통화별 소수 자릿수를 보존한다.
 *
 * [seq]는 SQLite가 부여하는 단조 증가 PK(클수록 최신)다 — KFTC 거래내역엔 행 id가 없어 mock이 부여한다.
 * 연속조회 커서의 유일 tiebreaker(같은 초 행도 누락 없이 seek)이자 합성 TransactionId의 충돌 없는 근거다.
 * 시드 히스토리를 **오래된 순으로** 먼저 적재하므로 seq 순서와 시간 순서가 일치하고, 세션 이체는 그 뒤에
 * 삽입돼 자동으로 더 큰 seq를 받는다(예전의 수동 seq 카운터가 필요 없다).
 *
 * [seeded]는 부팅 시 적재한 과거 거래(true)와 이번 세션 이체(false)를 구분한다.
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
