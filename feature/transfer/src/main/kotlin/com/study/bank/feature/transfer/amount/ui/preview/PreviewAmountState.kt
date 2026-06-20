package com.study.bank.feature.transfer.amount.ui.preview

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.amount.contract.AmountState
import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import java.math.BigDecimal

internal val PreviewAmountState = AmountState(
    source = AmountSourceUi(
        nickname = "U드림 저축예금",
        type = AccountTypeUi.SAVINGS,
        balance = MoneyUi(BigDecimal.valueOf(284_797), CurrencyUi.KRW),
    ),
    recipient = AmountRecipientUi(
        nickname = "종합매매 계좌",
        type = AccountTypeUi.CHECKING,
        bankDisplayName = "신한은행",
        numberMasked = "110-503-685417",
    ),
    amount = 0L,
)
