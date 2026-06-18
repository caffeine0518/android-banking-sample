package com.study.bank.data.remote.kftc.mock

/**
 * 원장 한 줄의 입출 방향. KFTC 와이어 문자열("입금"/"출금")로의 변환은 dispatcher 매퍼가 맡고,
 * 상태/원장은 의미만 들고 있어 와이어 표현에 결합되지 않는다.
 */
internal enum class TransactionDirection {
    DEPOSIT,
    WITHDRAWAL,
}
