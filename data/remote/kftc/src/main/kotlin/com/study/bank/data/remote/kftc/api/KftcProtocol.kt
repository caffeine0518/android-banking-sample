package com.study.bank.data.remote.kftc.api

/**
 * KFTC 오픈뱅킹 v2.0 와이어 계약값의 단일 소유처.
 *
 * 서버(mock)가 응답에 넣는 문자열과 클라(:data 매퍼)가 해석하는 문자열이 같아야 하므로 한 곳에서만
 * 정의한다. 양쪽에 따로 적으면 서버가 코드를 바꿀 때 한쪽만 고쳐도 컴파일은 통과한다.
 */

// envelope/payload에 그대로 포함되는 고정값.
const val RSP_SUCCESS = "A0000"
const val RSP_ERROR = "A0001"
const val BANK_RSP_OK = "000"

// 예금주구분: P=개인, B=법인. mock은 전부 개인.
const val HOLDER_TYPE_PERSONAL = "P"

// 거래내역 통장 표기. KFTC inout_type/tran_type 그대로 노출하는 한글 라벨.
const val INOUT_DEPOSIT = "입금"
const val INOUT_WITHDRAW = "출금"
const val TRAN_TYPE_TRANSFER = "이체"

// 출금이체 업무 거절 시 bank_rsp_code(은행 응답코드). KFTC는 업무 거절을 HTTP 200 + rsp A0001 + 이 코드로 알린다.
const val BANK_RSP_INSUFFICIENT_FUNDS = "311" // 출금계좌 잔액 부족
const val BANK_RSP_CURRENCY_MISMATCH = "320" // mock 가드: 내부 이체 통화 불일치
const val BANK_RSP_RECIPIENT_NOT_FOUND = "012" // 계좌실명조회: 수취 계좌 없음

// 계좌실명조회 예금주 상태. mock 확장값 — 휴면/해지를 RecipientLookup.Inactive로 구분.
const val ACCOUNT_STATUS_ACTIVE = "ACTIVE"
const val ACCOUNT_STATUS_INACTIVE = "INACTIVE"
