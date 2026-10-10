package com.study.bank.feature.transfer.result.contract

import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi

sealed interface ResultAction

sealed interface ResultIntent : ResultAction {
    data object BackClicked : ResultIntent

    data object ConfirmClicked : ResultIntent

    data object RetryClicked : ResultIntent
}

internal sealed interface ResultInternalAction : ResultAction {
    data class HeaderReady(val header: ResultHeaderUi) : ResultInternalAction
    data class Finished(val phase: ResultPhase) : ResultInternalAction
}
