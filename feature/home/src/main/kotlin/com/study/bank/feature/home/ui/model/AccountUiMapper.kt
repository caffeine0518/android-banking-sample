package com.study.bank.feature.home.ui.model

import com.study.bank.core.ui.mapper.toUi
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountType

internal fun Account.toUi(): AccountUi = AccountUi(
    id = id.value,
    bankDisplayName = bankCode.displayName,
    type = type.toUi(),
    nickname = nickname,
    balance = balance.toUi(),
)

internal fun AccountType.toUi(): AccountTypeUi = when (this) {
    AccountType.CHECKING -> AccountTypeUi.CHECKING
    AccountType.SAVINGS -> AccountTypeUi.SAVINGS
    AccountType.DEPOSIT -> AccountTypeUi.DEPOSIT
}
