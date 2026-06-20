package com.study.bank.feature.transfer.recipient.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.domain.model.account.AccountType
import com.study.bank.feature.transfer.R

enum class AccountTypeUi {
    CHECKING,
    SAVINGS,
    DEPOSIT,
}

/** 도메인 계좌 타입 → 송금 피쳐 UI 타입. recipient/amount 화면이 공유한다. */
internal fun AccountType.toAccountTypeUi(): AccountTypeUi = when (this) {
    AccountType.CHECKING -> AccountTypeUi.CHECKING
    AccountType.SAVINGS -> AccountTypeUi.SAVINGS
    AccountType.DEPOSIT -> AccountTypeUi.DEPOSIT
}

@Composable
internal fun AccountTypeUi.label(): String = stringResource(
    when (this) {
        AccountTypeUi.CHECKING -> R.string.transfer_account_type_checking
        AccountTypeUi.SAVINGS -> R.string.transfer_account_type_savings
        AccountTypeUi.DEPOSIT -> R.string.transfer_account_type_deposit
    },
)
