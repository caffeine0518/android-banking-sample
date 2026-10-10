package com.study.bank.data.remote.kftc.mock.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mock 은행이 보유한 계좌 한 행(`mock_accounts`). 시드로 적재되고, 이후에는 이체로 [balanceAmt]만 바뀐다.
 *
 * 한 행으로 KFTC `list_finuse` 항목과 `balance/fin_num` 응답을 모두 만든다.
 * [balanceAmt]는 KFTC 형식대로 소수점을 포함한 문자열이다(KRW: "2847320", USD: "3245.80"). 문자열의 소수 자릿수가
 * 곧 통화의 소수 자릿수이므로, 갱신할 때 같은 scale로 포맷하면 별도 scale 컬럼이 필요 없다.
 * [accountNum](전체)과 [accountNumMasked] 모두 출금이체의 수취계좌 매칭(내부 이체 판정)에 쓴다.
 * 앱은 list_finuse에서 마스킹된 번호만 받아 내 계좌끼리도 마스킹된 번호로 송금하기 때문이다.
 */
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
    @ColumnInfo(name = "account_num_masked")
    val accountNumMasked: String,
    @ColumnInfo(name = "account_alias")
    val accountAlias: String?,
    @ColumnInfo(name = "account_holder_name")
    val accountHolderName: String,
    @ColumnInfo(name = "account_type")
    val accountType: String,
    @ColumnInfo(name = "balance_amt")
    val balanceAmt: String,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
)
