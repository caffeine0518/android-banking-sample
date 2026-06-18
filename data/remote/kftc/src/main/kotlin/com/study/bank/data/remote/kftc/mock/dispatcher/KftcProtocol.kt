package com.study.bank.data.remote.kftc.mock.dispatcher

// KFTC 오픈뱅킹 v2.0이 envelope/payload에 그대로 박아 보내는 고정값들.
internal const val RSP_SUCCESS = "A0000"
internal const val RSP_ERROR = "A0001"
internal const val BANK_RSP_OK = "000"

// 예금주구분: P=개인, B=법인. Mock은 전부 개인.
internal const val HOLDER_TYPE_PERSONAL = "P"

// 사용자 일련번호. 실서비스에선 OAuth 토큰에서 유도되지만 mock은 고정.
internal const val USER_SEQ_NO = "1100000001"

// 거래내역 통장 표기. KFTC inout_type/tran_type 그대로 노출하는 한글 라벨.
internal const val INOUT_DEPOSIT = "입금"
internal const val INOUT_WITHDRAW = "출금"
internal const val TRAN_TYPE_TRANSFER = "이체"

// 출금이체 업무 거절 시 bank_rsp_code(은행 응답코드). KFTC는 업무 거절을 HTTP 200 + rsp A0001 + 이 코드로 알린다.
internal const val BANK_RSP_INSUFFICIENT_FUNDS = "311" // 출금계좌 잔액 부족
internal const val BANK_RSP_CURRENCY_MISMATCH = "320" // mock 가드: 내부 이체 통화 불일치
internal const val BANK_RSP_RECIPIENT_NOT_FOUND = "012" // 계좌실명조회: 수취 계좌 없음

// 계좌실명조회 예금주 상태. mock 확장값 — 휴면/해지를 RecipientLookup.Inactive로 구분.
internal const val ACCOUNT_STATUS_ACTIVE = "ACTIVE"
internal const val ACCOUNT_STATUS_INACTIVE = "INACTIVE"
