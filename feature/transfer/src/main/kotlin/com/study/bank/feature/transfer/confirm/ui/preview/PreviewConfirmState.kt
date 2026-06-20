package com.study.bank.feature.transfer.confirm.ui.preview

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.confirm.contract.ConfirmState
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import java.math.BigDecimal

internal val PreviewConfirmState = ConfirmState(
    detail = ConfirmDetailUi(
        recipientHolderName = "집주인",
        amount = MoneyUi(BigDecimal.valueOf(2), CurrencyUi.KRW),
        displayName = "강남규",
        sourceNickname = "U드림 저축예금 (인터넷전용)",
        sourceType = AccountTypeUi.SAVINGS,
        recipientBankDisplayName = "신한은행",
        recipientNumberMasked = "110-503-685417",
    ),
)
