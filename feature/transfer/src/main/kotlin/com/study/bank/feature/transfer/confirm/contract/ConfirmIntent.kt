package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.domain.model.account.Account

sealed interface ConfirmAction

sealed interface ConfirmIntent : ConfirmAction {
    data object BackClicked : ConfirmIntent

    data object SendClicked : ConfirmIntent
}

internal sealed interface ConfirmInternalAction : ConfirmAction {
    data class SourceUpdated(val source: Account?) : ConfirmInternalAction
}
