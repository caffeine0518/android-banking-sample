package com.study.bank.feature.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.money.format
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account

@Composable
internal fun TotalBalanceHeader(
    accounts: List<Account>,
    modifier: Modifier = Modifier,
) {
    val totalsByCurrency = remember(accounts) { accounts.totalsByCurrency() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text(
            text = "총 자산",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        totalsByCurrency.forEach { (_, total) ->
            Text(
                text = total.format(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun List<Account>.totalsByCurrency(): Map<Currency, Money> =
    groupBy { it.balance.currency }
        .mapValues { (currency, accounts) ->
            accounts.fold(Money.zero(currency)) { acc, account -> acc + account.balance }
        }
