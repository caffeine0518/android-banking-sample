package com.study.bank.data.remote.mock.dispatcher

// KFTC 오픈뱅킹 v2.0이 envelope/payload에 그대로 박아 보내는 고정값들.
internal const val RSP_SUCCESS = "A0000"
internal const val RSP_ERROR = "A0001"
internal const val BANK_RSP_OK = "000"

// 예금주구분: P=개인, B=법인. Mock은 전부 개인.
internal const val HOLDER_TYPE_PERSONAL = "P"

// 사용자 일련번호. 실서비스에선 OAuth 토큰에서 유도되지만 mock은 고정.
internal const val USER_SEQ_NO = "1100000001"
