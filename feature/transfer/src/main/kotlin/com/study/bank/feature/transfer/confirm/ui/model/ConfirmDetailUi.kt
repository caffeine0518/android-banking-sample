package com.study.bank.feature.transfer.confirm.ui.model

import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi

/** 송금 확인 화면에 보여줄 확정 직전 정보 묶음. */
data class ConfirmDetailUi(
    /** 제목 "○○○님에게"의 수취인 이름(계좌 명의). */
    val recipientHolderName: String,
    /** 보낼 금액(출금계좌 통화). */
    val amount: MoneyUi,
    /** "받는 분에게 표시" — 수취인 거래내역에 보일 이름. 기본값은 보내는 사람 명의. */
    val displayName: String,
    /** "출금 계좌" — 별명 없으면 타입 라벨로 폴백. */
    val sourceNickname: String?,
    val sourceType: AccountTypeUi,
    /** "입금 계좌" — 수취 은행·계좌번호. */
    val recipientBankDisplayName: String,
    val recipientNumberMasked: String,
)
