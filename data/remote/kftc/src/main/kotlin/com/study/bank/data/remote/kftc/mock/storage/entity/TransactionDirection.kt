package com.study.bank.data.remote.kftc.mock.storage.entity

/**
 * 원장 한 행의 입출금 방향. KFTC 응답 문자열("입금"/"출금")로의 변환은 매퍼가 담당하므로
 * 원장은 응답 표현에 의존하지 않는다.
 */
internal enum class TransactionDirection {
    DEPOSIT,
    WITHDRAWAL,
}
