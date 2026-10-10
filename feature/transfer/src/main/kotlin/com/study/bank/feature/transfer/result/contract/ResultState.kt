package com.study.bank.feature.transfer.result.contract

import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi

data class ResultState(
    val header: ResultHeaderUi? = null,
    val phase: ResultPhase = ResultPhase.Loading,
)

sealed interface ResultPhase {
    data object Loading : ResultPhase
    data object Success : ResultPhase
    data class Failure(val reason: ResultFailureUi) : ResultPhase
}
