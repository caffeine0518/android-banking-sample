package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.domain.model.account.Account

sealed interface ConfirmAction

sealed interface ConfirmIntent : ConfirmAction {
    data object BackClicked : ConfirmIntent

    /** "받는 분에게 표시" 행(수취인에게 노출될 이름 편집). 편집 화면 미구현이라 placeholder. */
    data object DisplayNameClicked : ConfirmIntent

    /** "출금 계좌" 행(출금계좌 변경). 변경 화면 미구현이라 placeholder. */
    data object SourceAccountClicked : ConfirmIntent

    /** "보내기" 버튼 → 송금 확정. */
    data object SendClicked : ConfirmIntent
}

internal sealed interface ConfirmInternalAction : ConfirmAction {
    /** 출금계좌 갱신. 수취인·금액은 라우트로 확정돼 고정이라 여기 싣지 않는다. */
    data class SourceUpdated(val source: Account?) : ConfirmInternalAction
}
