package com.study.bank.domain.model

enum class BankCode(val code: String, val displayName: String) {
    TOSS("092", "토스뱅크"),
    KAKAO("090", "카카오뱅크"),
    KB("004", "KB국민은행"),
    SHINHAN("088", "신한은행"),
    WOORI("020", "우리은행"),
    HANA("081", "하나은행"),
    NH("011", "농협은행"),
    IBK("003", "기업은행"),
    ;

    companion object {
        fun byCode(code: String): BankCode? = entries.firstOrNull { it.code == code }
    }
}
