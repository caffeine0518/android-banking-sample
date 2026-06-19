package com.study.bank.feature.transfer.recipient.ui.model

import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountUiMapper @Inject constructor() {

    fun map(account: Account): AccountUi = AccountUi(
        id = account.id.value,
        bankDisplayName = account.bankCode.displayName,
        type = mapType(account.type),
        nickname = account.nickname,
        numberMasked = account.number.value,
    )

    private fun mapType(type: AccountType): AccountTypeUi = when (type) {
        AccountType.CHECKING -> AccountTypeUi.CHECKING
        AccountType.SAVINGS -> AccountTypeUi.SAVINGS
        AccountType.DEPOSIT -> AccountTypeUi.DEPOSIT
    }
}
