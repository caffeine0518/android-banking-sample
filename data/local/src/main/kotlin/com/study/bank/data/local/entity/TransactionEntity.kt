package com.study.bank.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 거래내역 한 줄의 로컬 캐시(SSOT) 레코드.
 *
 * 금액/잔액은 통화별 소수 자릿수를 보존하는 문자열, 통화/타입/상태는 enum/code를 평탄화한 문자열,
 * 발생시각은 epoch milli로 저장한다. `amount`와 `balance_after`는 같은 `currency`를 공유한다.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "account_id")
    val accountId: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "amount")
    val amount: String,
    @ColumnInfo(name = "currency")
    val currency: String,
    @ColumnInfo(name = "balance_after")
    val balanceAfter: String,
    @ColumnInfo(name = "counterparty_name")
    val counterpartyName: String?,
    @ColumnInfo(name = "memo")
    val memo: String?,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
    @ColumnInfo(name = "status")
    val status: String,
)
