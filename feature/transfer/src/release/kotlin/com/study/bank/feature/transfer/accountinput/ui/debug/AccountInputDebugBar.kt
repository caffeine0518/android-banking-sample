package com.study.bank.feature.transfer.accountinput.ui.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.study.bank.domain.model.BankCode

/**
 * 릴리스 stub. 디버그 전용 해피패스 프리셋은 릴리스 빌드에 노출되지 않는다
 * (디버그 소스셋의 동명 함수가 실제 칩을 렌더링).
 */
@Composable
internal fun AccountInputDebugBar(
    onApplyPreset: (accountNumber: String, bank: BankCode) -> Unit,
    modifier: Modifier = Modifier,
) = Unit
