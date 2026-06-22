package com.study.bank.feature.home.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.format
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.home.R
import com.study.bank.feature.home.ui.model.AccountTypeUi
import com.study.bank.feature.home.ui.model.AccountUi

@Composable
internal fun AccountListItem(
    account: AccountUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // 표시명("월급통장")이 아니라 안정적 id로 테스트가 지목 — 서버가 이름을 바꿔도 안 깨짐.
            .testTag(BankTestTags.accountItem(account.id))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val typeLabel = account.type.label()
                Text(
                    text = stringResource(
                        R.string.home_account_subtitle,
                        account.bankDisplayName,
                        typeLabel,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = account.nickname ?: typeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = account.balance.format(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AccountTypeUi.label(): String = stringResource(
    when (this) {
        AccountTypeUi.CHECKING -> R.string.home_account_type_checking
        AccountTypeUi.SAVINGS -> R.string.home_account_type_savings
        AccountTypeUi.DEPOSIT -> R.string.home_account_type_deposit
    },
)
