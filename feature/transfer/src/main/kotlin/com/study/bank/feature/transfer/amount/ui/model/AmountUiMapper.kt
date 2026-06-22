package com.study.bank.feature.transfer.amount.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.account.Account
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import com.study.bank.feature.transfer.recipient.ui.model.toAccountTypeUi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmountUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    fun mapSource(account: Account): AmountSourceUi = AmountSourceUi(
        nickname = account.nickname,
        type = account.type.toAccountTypeUi(),
        balance = moneyUiMapper.map(account.balance),
    )

    fun mapRecipient(recipient: TransferRecipientArg): AmountRecipientUi = AmountRecipientUi(
        holderName = recipient.holderName,
        bankDisplayName = recipient.bankDisplayName,
        accountNumber = recipient.accountNumber,
    )
}
