package com.study.bank.feature.account.contract

import com.study.bank.feature.account.ui.model.AccountUi
import com.study.bank.feature.account.ui.model.TransactionUi

data class AccountDetailState(
    val account: AccountUi? = null,
    val transactions: List<TransactionUi> = emptyList(),
    val isLoading: Boolean = false,
)
