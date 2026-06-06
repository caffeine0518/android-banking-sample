package com.study.bank.feature.home.ui.preview

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType

internal val PreviewAccounts = listOf(
    Account(
        id = AccountId("acc-1"),
        number = AccountNumber("1000123456789"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of(2_847_320L, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = "월급통장",
    ),
    Account(
        id = AccountId("acc-2"),
        number = AccountNumber("1000987654321"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of("3245.80", Currency.USD),
        type = AccountType.CHECKING,
        nickname = "외화통장 USD",
    ),
    Account(
        id = AccountId("acc-3"),
        number = AccountNumber("1000555544443"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of(12_000_000L, Currency.KRW),
        type = AccountType.SAVINGS,
        nickname = "세이프박스",
    ),
    Account(
        id = AccountId("acc-4"),
        number = AccountNumber("110234567890"),
        bankCode = BankCode.SHINHAN,
        holderName = "홍길동",
        balance = Money.of(450_000L, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = null,
    ),
)
