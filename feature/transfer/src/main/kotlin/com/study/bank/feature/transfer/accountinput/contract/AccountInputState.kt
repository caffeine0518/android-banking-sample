package com.study.bank.feature.transfer.accountinput.contract

import com.study.bank.domain.model.BankCode

data class AccountInputState(
    val accountNumber: String = "",
    val selectedBank: BankCode = BankCode.KAKAO,
    val isBankPickerVisible: Boolean = false,
    val isResolving: Boolean = false,
    val error: AccountInputError? = null,
) {
    val isConfirmEnabled: Boolean get() = accountNumber.isNotBlank() && !isResolving
}
