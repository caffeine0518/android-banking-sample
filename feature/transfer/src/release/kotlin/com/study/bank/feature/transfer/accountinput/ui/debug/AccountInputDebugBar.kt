package com.study.bank.feature.transfer.accountinput.ui.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.study.bank.domain.model.BankCode

/** 릴리스에는 디버그 프리셋을 노출하지 않는다. */
@Composable
internal fun AccountInputDebugBar(
    onApplyPreset: (accountNumber: String, bank: BankCode) -> Unit,
    modifier: Modifier = Modifier,
) = Unit
