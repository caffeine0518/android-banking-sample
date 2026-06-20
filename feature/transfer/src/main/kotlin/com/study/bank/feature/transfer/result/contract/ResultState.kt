package com.study.bank.feature.transfer.result.contract

import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi

/**
 * 송금 결과 화면 상태. 한 화면이 [phase]에 따라 로딩 → 성공/실패로 전환된다.
 * [header](수취 명의·금액)는 계좌가 로딩되는 즉시 채워져 성공/실패 제목에 함께 쓰인다.
 */
data class ResultState(
    val header: ResultHeaderUi? = null,
    val phase: ResultPhase = ResultPhase.Loading,
)

sealed interface ResultPhase {
    data object Loading : ResultPhase
    data object Success : ResultPhase
    data class Failure(val reason: ResultFailureUi) : ResultPhase
}
