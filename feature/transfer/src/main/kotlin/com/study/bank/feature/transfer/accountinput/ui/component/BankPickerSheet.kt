package com.study.bank.feature.transfer.accountinput.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.R

/**
 * 은행 선택 바텀시트 콘텐츠. 송금 가능한 은행을 3열 그리드로 보여준다.
 * (로고 자산이 없어 은행명만 표기 — 실제 토스는 로고를 함께 노출.)
 */
@Composable
internal fun BankPickerSheet(
    banks: List<BankCode>,
    selected: BankCode,
    onSelect: (BankCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.transfer_account_input_bank_picker_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        // 8개뿐이라 LazyVerticalGrid 대신 3개씩 끊어 단순 Row로 깐다(시트 높이 측정 이슈 회피).
        banks.chunked(COLUMNS).forEach { rowBanks ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowBanks.forEach { bank ->
                    BankCell(
                        bank = bank,
                        isSelected = bank == selected,
                        onClick = { onSelect(bank) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // 마지막 줄이 3칸 미만이면 빈 칸으로 채워 정렬을 유지.
                repeat(COLUMNS - rowBanks.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BankCell(
    bank: BankCode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier
            .height(72.dp)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = bank.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val COLUMNS = 3
