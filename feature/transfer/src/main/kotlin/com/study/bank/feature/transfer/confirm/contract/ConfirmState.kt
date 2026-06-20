package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi

/**
 * 송금 확인 화면 상태.
 *
 * [detail]은 출금·수취 계좌 + 금액이 모두 로딩되면 채워진다. null인 동안은 "보내기" 비활성.
 */
data class ConfirmState(
    val detail: ConfirmDetailUi? = null,
)
