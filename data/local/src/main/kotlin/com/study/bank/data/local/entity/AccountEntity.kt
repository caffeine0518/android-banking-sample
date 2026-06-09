package com.study.bank.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "number")
    val number: String,
    @ColumnInfo(name = "bank_code")
    val bankCode: String,
    @ColumnInfo(name = "holder_name")
    val holderName: String,
    @ColumnInfo(name = "balance_amount")
    val balanceAmount: String,
    @ColumnInfo(name = "balance_currency")
    val balanceCurrency: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "nickname")
    val nickname: String?,
)
