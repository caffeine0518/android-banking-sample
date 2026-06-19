package com.study.bank.feature.transfer.recipient.ui.preview

import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountUi

internal val PreviewRecipientState = RecipientState(
    myAccounts = listOf(
        AccountUi(
            id = "acc-2",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "외화통장 USD",
            numberMasked = "1000-98-***4321",
        ),
        AccountUi(
            id = "acc-3",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.SAVINGS,
            nickname = "세이프박스",
            numberMasked = "1000-55-***4443",
        ),
        AccountUi(
            id = "acc-4",
            bankDisplayName = "신한은행",
            type = AccountTypeUi.CHECKING,
            nickname = null,
            numberMasked = "110-23-***7890",
        ),
    ),
)
