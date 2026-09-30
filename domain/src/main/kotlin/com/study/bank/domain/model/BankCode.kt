package com.study.bank.domain.model

enum class BankCode(val code: String, val displayName: String) {
    TOSS(code = "092", displayName = "토스뱅크"),
    KAKAO(code = "090", displayName = "카카오뱅크"),
    KB(code = "004", displayName = "KB국민은행"),
    SHINHAN(code = "088", displayName = "신한은행"),
    WOORI(code = "020", displayName = "우리은행"),
    HANA(code = "081", displayName = "하나은행"),
    NH(code = "011", displayName = "농협은행"),
    IBK(code = "003", displayName = "기업은행"),
    ;

    companion object {
        fun byCode(code: String): BankCode? = entries.firstOrNull { it.code == code }
    }
}
