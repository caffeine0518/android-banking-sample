package com.study.bank.feature.transfer.accountinput.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface AccountInputEffect {
    data object NavigateBack : AccountInputEffect

    data class NavigateToAmount(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
    ) : AccountInputEffect
}
