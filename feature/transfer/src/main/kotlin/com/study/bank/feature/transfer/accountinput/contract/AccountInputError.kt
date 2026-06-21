package com.study.bank.feature.transfer.accountinput.contract

/** 실명조회 결과를 입력 화면에 노출할 오류 종류. */
enum class AccountInputError {
    NOT_FOUND,
    INACTIVE,
    SELF_TRANSFER,
    NETWORK,
    UNKNOWN,
}
