package com.study.bank.feature.transfer.result.contract

import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi

sealed interface ResultAction

sealed interface ResultIntent : ResultAction {
    /** 송금은 이미 시도됐으므로 플로우를 종료한다. */
    data object BackClicked : ResultIntent

    data object ConfirmClicked : ResultIntent

    data object RetryClicked : ResultIntent
}

internal sealed interface ResultInternalAction : ResultAction {
    data class HeaderReady(val header: ResultHeaderUi) : ResultInternalAction
    data class Finished(val phase: ResultPhase) : ResultInternalAction
}
