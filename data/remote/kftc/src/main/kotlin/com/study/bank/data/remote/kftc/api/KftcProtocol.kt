package com.study.bank.data.remote.kftc.api

/**
 * KFTC 오픈뱅킹 v2.0 응답 코드와 고정 문자열.
 *
 * mock 서버가 응답에 넣는 값과 :data 매퍼가 해석하는 값이 같아야 하므로 이 파일에서만 정의한다.
 * 양쪽에 따로 정의하면 한쪽만 바꿔도 컴파일이 통과해 불일치를 검출하지 못한다.
 */

// 응답 코드 정상값과 공통 오류값.
const val RSP_SUCCESS = "A0000"
const val RSP_ERROR = "A0001"
const val BANK_RSP_OK = "000"

// 예금주 구분: P=개인, B=법인. mock 계좌는 모두 개인이다.
const val HOLDER_TYPE_PERSONAL = "P"

// 거래내역의 입출금 구분(inout_type)과 거래 구분(tran_type). KFTC 응답의 한글 값 그대로다.
const val INOUT_DEPOSIT = "입금"
const val INOUT_WITHDRAW = "출금"
const val TRAN_TYPE_TRANSFER = "이체"

// 출금이체 업무 거절 코드(bank_rsp_code). KFTC는 업무 거절을 HTTP 200 + rsp_code A0001 + 이 코드로 반환한다.
const val BANK_RSP_INSUFFICIENT_FUNDS = "311" // 출금계좌 잔액 부족
const val BANK_RSP_CURRENCY_MISMATCH = "320" // 통화가 다른 계좌로 이체 (mock 전용)
const val BANK_RSP_RECIPIENT_NOT_FOUND = "012" // 수취 계좌 없음 (계좌실명조회)

// 계좌실명조회의 예금주 상태(mock 확장 필드). 휴면·해지 계좌를 RecipientLookup.Inactive로 구분한다.
const val ACCOUNT_STATUS_ACTIVE = "ACTIVE"
const val ACCOUNT_STATUS_INACTIVE = "INACTIVE"
