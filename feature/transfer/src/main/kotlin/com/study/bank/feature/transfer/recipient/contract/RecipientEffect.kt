package com.study.bank.feature.transfer.recipient.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface RecipientEffect {
    data object NavigateBack : RecipientEffect

    data class NavigateToAccountNumberInput(val sourceAccountId: String) : RecipientEffect

    data class NavigateToAmount(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
    ) : RecipientEffect
}
