package com.study.bank.data.remote.kftc.mock

/**
 * Mock 서버가 응답을 구성할 때 쓰는 시드 레코드.
 *
 * 한 [SeedAccount]가 KFTC `list_finuse` 항목과 `balance/fin_num` 응답 양쪽을 채운다.
 * `balanceAmt`는 KFTC 그대로 소수점 포함 문자열 (KRW: "2847320", USD: "3245.80").
 */
internal data class SeedAccount(
    val fintechUseNum: String,
    val bankCodeStd: String,
    val bankName: String,
    val accountNumMasked: String,
    val accountAlias: String?,
    val accountHolderName: String,
    val accountType: String,
    val balanceAmt: String,
    val currencyCode: String,
    val productName: String,
)
