package com.study.bank.feature.transfer.result.ui.model

import com.study.bank.core.ui.model.MoneyUi

/** "○○○님에게 N원을 …" 제목에 쓰는 수취 명의 + 금액. 성공·실패 공통. */
data class ResultHeaderUi(
    val recipientName: String,
    val amount: MoneyUi,
)
