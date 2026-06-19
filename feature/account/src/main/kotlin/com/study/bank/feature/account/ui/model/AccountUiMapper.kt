package com.study.bank.feature.account.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    fun map(account: Account): AccountUi = AccountUi(
        id = account.id.value,
        bankDisplayName = account.bankCode.displayName,
        type = mapType(account.type),
        nickname = account.nickname,
        numberMasked = account.number.value,
        balance = moneyUiMapper.map(account.balance),
    )

    private fun mapType(type: AccountType): AccountTypeUi = when (type) {
        AccountType.CHECKING -> AccountTypeUi.CHECKING
        AccountType.SAVINGS -> AccountTypeUi.SAVINGS
        AccountType.DEPOSIT -> AccountTypeUi.DEPOSIT
    }
}
