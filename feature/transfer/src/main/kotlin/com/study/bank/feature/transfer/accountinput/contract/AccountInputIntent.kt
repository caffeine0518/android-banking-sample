package com.study.bank.feature.transfer.accountinput.contract

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.transfer.RecipientValidation

sealed interface AccountInputAction

sealed interface AccountInputIntent : AccountInputAction {
    data object BackClicked : AccountInputIntent
    data class AccountNumberChanged(val value: String) : AccountInputIntent
    data object AccountNumberCleared : AccountInputIntent
    data object BankSelectorClicked : AccountInputIntent
    data object BankPickerDismissed : AccountInputIntent
    data class BankSelected(val bankCode: BankCode) : AccountInputIntent
    data object ConfirmClicked : AccountInputIntent
}

internal sealed interface AccountInputInternalAction : AccountInputAction {
    data class Resolved(val validation: RecipientValidation) : AccountInputInternalAction

    data object ResolveFailed : AccountInputInternalAction
}
