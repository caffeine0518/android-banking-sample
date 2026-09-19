package com.study.bank.data.remote.kftc.mock.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mock 은행이 보유한 계좌 한 행(`mock_accounts`). 시드로 적재되고, 이후 [balanceAmt]만 이체로 변한다.
 *
 * 한 행이 KFTC `list_finuse` 항목과 `balance/fin_num` 응답 양쪽을 채운다.
 * [balanceAmt]는 KFTC 그대로 소수점 포함 문자열 (KRW: "2847320", USD: "3245.80") — 문자열의 소수 자릿수가
 * 곧 통화 exponent라, 갱신할 때 같은 scale로 다시 포맷하면 별도 scale 컬럼이 필요 없다.
 * [accountNum](전체)·[accountNumMasked] 둘 다 출금이체 수취계좌 매칭(내부 이체 판정)에 쓰인다 —
 * 앱은 list_finuse에서 마스킹 번호만 받아 내 계좌끼리도 마스킹 번호로 송금하기 때문.
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
