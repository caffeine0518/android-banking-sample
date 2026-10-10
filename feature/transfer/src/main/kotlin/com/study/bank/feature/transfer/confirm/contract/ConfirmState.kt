package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi

data class ConfirmState(
    val detail: ConfirmDetailUi? = null,
    val submitting: Boolean = false,
)
