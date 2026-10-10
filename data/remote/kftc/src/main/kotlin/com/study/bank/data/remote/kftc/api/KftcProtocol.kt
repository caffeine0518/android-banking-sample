package com.study.bank.data.remote.kftc.api

// mock 서버와 :data 매퍼가 같은 값을 쓰도록 여기서만 정의한다.

const val RSP_SUCCESS = "A0000"
const val RSP_ERROR = "A0001"
const val BANK_RSP_OK = "000"

const val HOLDER_TYPE_PERSONAL = "P"

const val INOUT_DEPOSIT = "입금"
const val INOUT_WITHDRAW = "출금"
const val TRAN_TYPE_TRANSFER = "이체"

// 업무 거절은 HTTP 200 + rsp_code A0001 + 아래 bank_rsp_code로 반환된다.
const val BANK_RSP_INSUFFICIENT_FUNDS = "311" // 출금계좌 잔액 부족
const val BANK_RSP_CURRENCY_MISMATCH = "320" // 통화가 다른 계좌로 이체 (mock 전용)
const val BANK_RSP_RECIPIENT_NOT_FOUND = "012" // 수취 계좌 없음 (계좌실명조회)

// 계좌실명조회 account_status(mock 확장 필드).
const val ACCOUNT_STATUS_ACTIVE = "ACTIVE"
const val ACCOUNT_STATUS_INACTIVE = "INACTIVE"
