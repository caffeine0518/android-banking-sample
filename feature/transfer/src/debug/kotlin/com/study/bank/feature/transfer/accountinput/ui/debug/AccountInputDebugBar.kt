package com.study.bank.feature.transfer.accountinput.ui.debug

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.R

/** 앱에는 마스킹 번호만 보여 실명조회에 맞는 전체 계좌번호를 알 수 없으므로, 해피패스 수취 계좌를 한 번에 입력한다. */
@Composable
internal fun AccountInputDebugBar(
    onApplyPreset: (accountNumber: String, bank: BankCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = { onApplyPreset(HAPPY_PATH_ACCOUNT_NUMBER, HAPPY_PATH_BANK) },
        label = { Text(stringResource(R.string.transfer_debug_fill_happy_path_account)) },
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

// 세이프박스(토스뱅크 KRW). KRW 계좌에서 보내면 송금 성공까지 이어진다.
private const val HAPPY_PATH_ACCOUNT_NUMBER = "1000551114443"
private val HAPPY_PATH_BANK = BankCode.TOSS
