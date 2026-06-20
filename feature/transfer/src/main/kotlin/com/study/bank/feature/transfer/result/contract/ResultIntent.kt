package com.study.bank.feature.transfer.result.contract

import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi

sealed interface ResultAction

sealed interface ResultIntent : ResultAction {
    /** 상단 백 버튼. 송금은 이미 시도됐으므로 플로우를 종료한다. */
    data object BackClicked : ResultIntent

    /** "확인" 버튼 → 플로우 종료. */
    data object ConfirmClicked : ResultIntent

    /** "공유하기"(성공) → 공유. 공유 시트 미구현이라 placeholder. */
    data object ShareClicked : ResultIntent

    /** "메모 남기기"(성공) → 메모 편집. 미구현이라 placeholder. */
    data object LeaveMemoClicked : ResultIntent

    /** "다시 시도"(실패) → 송금 재시도. */
    data object RetryClicked : ResultIntent
}

internal sealed interface ResultInternalAction : ResultAction {
    data class HeaderReady(val header: ResultHeaderUi) : ResultInternalAction
    data class Finished(val phase: ResultPhase) : ResultInternalAction
}
