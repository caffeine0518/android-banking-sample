package com.study.bank.feature.transfer.amount.ui.model

/**
 * 수취계좌("받는 분") 표시용. 본인 계좌·외부(타행) 계좌를 모두 표현하므로, 본인 계좌 전용 개념인
 * 별명/계좌종류 대신 실명조회로 확정된 예금주명·은행·계좌번호를 보여준다.
 */
data class AmountRecipientUi(
    val holderName: String,
    val bankDisplayName: String,
    val accountNumber: String,
)
