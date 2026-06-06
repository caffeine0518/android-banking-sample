package com.study.bank.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.component.AccountListItem
import com.study.bank.feature.home.ui.component.TotalBalanceHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("내 계좌") },
                actions = {
                    TextButton(onClick = { onIntent(HomeIntent.Refresh) }) {
                        Text("새로고침")
                    }
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            HomeContent(
                state = state,
                onAccountClick = { onIntent(HomeIntent.AccountClicked(it)) },
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onAccountClick: (AccountId) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TotalBalanceHeader(accounts = state.accounts)
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        items(state.accounts, key = { it.id.value }) { account ->
            AccountListItem(
                account = account,
                onClick = { onAccountClick(account.id) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            state = HomeState(accounts = PREVIEW_ACCOUNTS),
            onIntent = {},
        )
    }
}

private val PREVIEW_ACCOUNTS = listOf(
    Account(
        id = AccountId("p1"),
        number = AccountNumber("1000123456789"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of(2_847_320L, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = "월급통장",
    ),
    Account(
        id = AccountId("p2"),
        number = AccountNumber("1000987654321"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of("3245.80", Currency.USD),
        type = AccountType.CHECKING,
        nickname = "외화통장 USD",
    ),
)
