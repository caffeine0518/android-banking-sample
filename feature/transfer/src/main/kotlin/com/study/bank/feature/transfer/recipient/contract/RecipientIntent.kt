package com.study.bank.feature.transfer.recipient.contract

import com.study.bank.domain.model.account.Account

sealed interface RecipientAction

sealed interface RecipientIntent : RecipientAction {
    data object BackClicked : RecipientIntent
    data object AccountNumberInputClicked : RecipientIntent
    data class MyAccountClicked(val accountId: String) : RecipientIntent
}

internal sealed interface RecipientInternalAction : RecipientAction {
    data class MyAccountsUpdated(val accounts: List<Account>) : RecipientInternalAction
}
