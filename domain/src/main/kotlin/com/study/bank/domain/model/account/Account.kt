package com.study.bank.domain.model.account

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Money

data class Account(
    val id: AccountId,
    /** 화면 표시용. 오픈뱅킹에서 받은 마스킹 번호다. */
    val number: AccountNumber,
    /** 송금용 전체 계좌번호. 자행 계정계에서만 받으므로 다른 은행 계좌는 null 이다. */
    val fullNumber: AccountNumber? = null,
    val bankCode: BankCode,
    val holderName: String,
    val balance: Money,
    val type: AccountType,
    val nickname: String? = null,
)
