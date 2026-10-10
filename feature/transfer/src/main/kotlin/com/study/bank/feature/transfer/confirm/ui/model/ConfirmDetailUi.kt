package com.study.bank.feature.transfer.confirm.ui.model

import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi

data class ConfirmDetailUi(
    val recipientHolderName: String,
    val amount: MoneyUi,
    /** 수취인 거래내역에 보일 이름. 기본값은 보내는 사람 명의다. */
    val displayName: String,
    val sourceNickname: String?,
    val sourceType: AccountTypeUi,
    val recipientBankDisplayName: String,
    val recipientNumberMasked: String,
)
