package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.navigation.TransferRecipientArg

sealed interface ConfirmEffect {
    data object NavigateBack : ConfirmEffect

    data class Submit(
        val sourceAccountId: String,
        val recipient: TransferRecipientArg,
        val amount: Long,
        val idempotencyKey: String,
    ) : ConfirmEffect
}
