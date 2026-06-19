package com.study.bank.feature.transfer.recipient.ui.model

/**
 * 수취인 선택 화면의 "내 계좌" 행 표시용. 잔액은 이 화면에서 쓰지 않아 담지 않는다.
 */
data class AccountUi(
    val id: String,
    val bankDisplayName: String,
    val type: AccountTypeUi,
    val nickname: String?,
    val numberMasked: String,
)
