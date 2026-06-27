package com.study.bank.data.remote.kftc.mock

/**
 * Mock 은행 상태가 누적하는 거래원장 한 줄.
 *
 * 이체 1건은 출금계좌에 [TransactionDirection.WITHDRAWAL], (내부 수취 시) 입금계좌에
 * [TransactionDirection.DEPOSIT] 레코드를 남긴다. 금액/잔액 문자열은 통화별 소수 자릿수를 보존한다.
 *
 * [seq]는 거래의 단조 증가 고유 식별자(클수록 최신). KFTC 기본 거래내역엔 행 id가 없어 mock이 부여한다 —
 * 연속조회 커서의 유일 tiebreaker(같은 초 행도 누락 없이 seek)이자, 합성 TransactionId의 충돌 없는 근거가 된다.
 */
internal data class TransactionRecord(
    val seq: Long,
    val tranDate: String,
    val tranTime: String,
    val direction: TransactionDirection,
    val printContent: String,
    val tranAmt: String,
    val afterBalanceAmt: String,
    val counterpartyName: String?,
)
