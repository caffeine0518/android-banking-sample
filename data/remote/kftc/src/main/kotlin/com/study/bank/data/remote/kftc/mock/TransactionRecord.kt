package com.study.bank.data.remote.kftc.mock

/**
 * Mock 은행 상태가 누적하는 거래원장 한 줄.
 *
 * 이체 1건은 출금계좌에 [TransactionDirection.WITHDRAWAL], (내부 수취 시) 입금계좌에
 * [TransactionDirection.DEPOSIT] 레코드를 남긴다. 금액/잔액 문자열은 통화별 소수 자릿수를 보존한다.
 */
internal data class TransactionRecord(
    val tranDate: String,
    val tranTime: String,
    val direction: TransactionDirection,
    val printContent: String,
    val tranAmt: String,
    val afterBalanceAmt: String,
    val counterpartyName: String?,
)
