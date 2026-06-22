package com.study.bank.feature.transfer.confirm.contract

import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi

/**
 * 송금 확인 화면 상태.
 *
 * [detail]은 출금·수취 계좌 + 금액이 모두 로딩되면 채워진다. null인 동안은 "보내기" 비활성.
 * [submitting]은 "보내기"를 한 번 누르면 true가 되어 이후 탭을 막는다 — 연타로 결과 화면이
 * 여러 번 push돼 각각 다른 멱등키로 이중 송금되는 것을 출처에서 차단한다.
 */
data class ConfirmState(
    val detail: ConfirmDetailUi? = null,
    val submitting: Boolean = false,
)
