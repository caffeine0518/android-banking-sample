package com.study.bank.feature.transfer.recipient.ui.model

import com.study.bank.domain.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountUiMapper @Inject constructor() {

    fun map(account: Account): AccountUi = AccountUi(
        id = account.id.value,
        bankDisplayName = account.bankCode.displayName,
        type = account.type.toAccountTypeUi(),
        nickname = account.nickname,
        numberMasked = account.number.value,
    )
}
