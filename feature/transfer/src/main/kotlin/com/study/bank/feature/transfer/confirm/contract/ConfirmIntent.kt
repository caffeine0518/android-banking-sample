package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.domain.model.account.Account

sealed interface ConfirmAction

sealed interface ConfirmIntent : ConfirmAction {
    data object BackClicked : ConfirmIntent

    data object SendClicked : ConfirmIntent
}

internal sealed interface ConfirmInternalAction : ConfirmAction {
    /** 출금계좌 갱신. 수취인·금액은 라우트로 확정돼 고정이라 여기 싣지 않는다. */
    data class SourceUpdated(val source: Account?) : ConfirmInternalAction
}
