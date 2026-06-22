package com.study.bank.feature.account.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.format
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.account.R
import com.study.bank.feature.account.ui.model.AccountTypeUi
import com.study.bank.feature.account.ui.model.AccountUi

@Composable
internal fun AccountDetailHeader(
    account: AccountUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // 헤더는 계좌 로딩 완료 후에만 그려지므로, 이 태그의 등장 = "해당 id 상세에 도착(로딩 완료)"이다.
            .testTag(BankTestTags.accountDetail(account.id))
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        val typeLabel = account.type.label()
        Text(
            text = stringResource(R.string.account_subtitle, account.bankDisplayName, typeLabel),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = account.nickname ?: typeLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = account.numberMasked,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = account.balance.format(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AccountTypeUi.label(): String = stringResource(
    when (this) {
        AccountTypeUi.CHECKING -> R.string.account_type_checking
        AccountTypeUi.SAVINGS -> R.string.account_type_savings
        AccountTypeUi.DEPOSIT -> R.string.account_type_deposit
    },
)
