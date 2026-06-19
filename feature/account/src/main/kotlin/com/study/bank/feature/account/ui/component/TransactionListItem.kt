package com.study.bank.feature.account.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.format
import com.study.bank.feature.account.R
import com.study.bank.feature.account.ui.model.TransactionTypeUi
import com.study.bank.feature.account.ui.model.TransactionUi

@Composable
internal fun TransactionListItem(
    transaction: TransactionUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.counterpartyName ?: transaction.type.label(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = transaction.occurredAtLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val inbound = transaction.type.isInbound
        Text(
            text = stringResource(
                if (inbound) R.string.account_tx_amount_inbound else R.string.account_tx_amount_outbound,
                transaction.amount.format(),
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (inbound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TransactionTypeUi.label(): String = stringResource(
    when (this) {
        TransactionTypeUi.DEPOSIT -> R.string.account_tx_type_deposit
        TransactionTypeUi.WITHDRAWAL -> R.string.account_tx_type_withdrawal
        TransactionTypeUi.TRANSFER_IN -> R.string.account_tx_type_transfer_in
        TransactionTypeUi.TRANSFER_OUT -> R.string.account_tx_type_transfer_out
    },
)
