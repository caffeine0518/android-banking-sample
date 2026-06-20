package com.study.bank.data.remote.kftc.mock

/**
 * Mock 서버가 응답을 구성할 때 쓰는 시드 레코드.
 *
 * 한 [SeedAccount]가 KFTC `list_finuse` 항목과 `balance/fin_num` 응답 양쪽을 채운다.
 * `balanceAmt`는 KFTC 그대로 소수점 포함 문자열 (KRW: "2847320", USD: "3245.80").
 * `accountNum`(전체)·`accountNumMasked` 둘 다 출금이체 수취계좌 매칭(내부 이체 판정)에 쓰인다 —
 * 앱은 list_finuse에서 마스킹 번호만 받아 내 계좌끼리도 마스킹 번호로 송금하기 때문.
 */
internal data class SeedAccount(
    val fintechUseNum: String,
    val bankCodeStd: String,
    val bankName: String,
    val accountNum: String,
    val accountNumMasked: String,
    val accountAlias: String?,
    val accountHolderName: String,
    val accountType: String,
    val balanceAmt: String,
    val currencyCode: String,
    val productName: String,
)
