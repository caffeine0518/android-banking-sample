package com.study.bank.feature.transfer.recipient.contract

import com.study.bank.feature.transfer.recipient.ui.model.AccountUi

data class RecipientState(
    val myAccounts: List<AccountUi> = emptyList(),
)
