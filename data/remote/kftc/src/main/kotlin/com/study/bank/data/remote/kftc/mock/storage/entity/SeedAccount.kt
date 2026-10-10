package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_accounts")
internal data class SeedAccount(
    @PrimaryKey
    @ColumnInfo(name = "fintech_use_num")
    val fintechUseNum: String,
    @ColumnInfo(name = "bank_code_std")
    val bankCodeStd: String,
    @ColumnInfo(name = "bank_name")
    val bankName: String,
    @ColumnInfo(name = "account_num")
    val accountNum: String,
    /** 앱은 마스킹된 번호만 받으므로 내 계좌끼리 송금할 때 수취계좌 매칭에 쓴다. */
    @ColumnInfo(name = "account_num_masked")
    val accountNumMasked: String,
    @ColumnInfo(name = "account_alias")
    val accountAlias: String?,
    @ColumnInfo(name = "account_holder_name")
    val accountHolderName: String,
    @ColumnInfo(name = "account_type")
    val accountType: String,
    /** 소수 자릿수가 통화의 소수 자릿수다. 갱신할 때 같은 scale로 포맷해 유지한다. */
    @ColumnInfo(name = "balance_amt")
    val balanceAmt: String,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
)
