package com.study.bank.feature.home.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.core.ui.model.format
import com.study.bank.feature.home.R

@Composable
internal fun TotalBalanceHeader(
    totalAssets: MoneyUi?,
    unconvertedAssets: List<MoneyUi>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.home_total_balance_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = totalAssets?.format().orEmpty(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        // 환산 불가 자산을 "+ £1,850.40" 형태의 추가 라인으로 — 총자산에 합산되는 자산임을 시각화.
        for (money in unconvertedAssets) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_unconverted_asset_item, money.format()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
