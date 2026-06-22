package com.study.bank.feature.transfer.confirm.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.feature.transfer.recipient.ui.model.toAccountTypeUi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfirmUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    /** 금액은 출금계좌 통화 최소단위(minor unit) 정수(키패드 입력)로 해석한다. */
    fun map(source: Account, recipient: Account, amount: Long): ConfirmDetailUi = ConfirmDetailUi(
        recipientHolderName = recipient.holderName,
        amount = moneyUiMapper.map(Money.ofMinor(amount, source.balance.currency)),
        displayName = source.holderName,
        sourceNickname = source.nickname,
        sourceType = source.type.toAccountTypeUi(),
        recipientBankDisplayName = recipient.bankCode.displayName,
        recipientNumberMasked = recipient.number.value,
    )
}
