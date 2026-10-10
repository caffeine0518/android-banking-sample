package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi

data class ConfirmState(
    val detail: ConfirmDetailUi? = null,
    /** 연타로 결과 화면이 여러 번 열리지 않도록 첫 탭 후 true가 된다. */
    val submitting: Boolean = false,
)
