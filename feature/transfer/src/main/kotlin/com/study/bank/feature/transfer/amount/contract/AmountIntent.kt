package com.study.bank.feature.transfer.amount.contract

import com.study.bank.domain.model.account.Account

sealed interface AmountAction

sealed interface AmountIntent : AmountAction {
    data object BackClicked : AmountIntent

    /** 키패드 숫자 입력. "1".."9", "0", "00". */
    data class DigitAppended(val digit: String) : AmountIntent

    data object BackspacePressed : AmountIntent

    data object FillBalanceClicked : AmountIntent

    data object NextClicked : AmountIntent
}

internal sealed interface AmountInternalAction : AmountAction {
    data class SourceUpdated(val source: Account?) : AmountInternalAction
}
