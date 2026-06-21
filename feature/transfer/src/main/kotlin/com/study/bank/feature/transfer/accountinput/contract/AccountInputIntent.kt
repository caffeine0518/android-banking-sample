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
    /** 실명조회 완료. 결과에 따라 금액 화면 이동 또는 오류 노출. */
    data class Resolved(val validation: RecipientValidation) : AccountInputInternalAction

    /** 실명조회 중 예외(네트워크 등). */
    data object ResolveFailed : AccountInputInternalAction
}
