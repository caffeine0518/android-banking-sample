package com.study.bank.feature.transfer.amount.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface AmountEffect {
    data object NavigateBack : AmountEffect

    data class NavigateNext(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
        val amount: Long,
    ) : AmountEffect
}
