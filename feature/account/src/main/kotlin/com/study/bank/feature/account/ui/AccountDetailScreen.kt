package com.study.bank.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.account.R
import com.study.bank.feature.account.contract.AccountDetailIntent
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.component.AccountDetailHeader
import com.study.bank.feature.account.ui.component.TransactionListItem
import com.study.bank.feature.account.ui.preview.PreviewAccountDetailState
import com.study.bank.feature.account.ui.preview.PreviewAccountDetailStateLongList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountDetailScreen(
    state: AccountDetailState,
    onIntent: (AccountDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(AccountDetailIntent.BackClicked) },
                        modifier = Modifier.testTag(BankTestTags.DETAIL_BACK),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.account_action_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onIntent(AccountDetailIntent.Refresh) }) {
                        Text(stringResource(R.string.account_action_refresh))
                    }
                },
            )
        },
        bottomBar = {
            SendButton(
                enabled = state.account != null,
                onClick = { onIntent(AccountDetailIntent.SendClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 계좌 (상단)
            state.account?.let { AccountDetailHeader(it) }
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                text = stringResource(R.string.account_transactions_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .testTag(BankTestTags.DETAIL_TX_LABEL)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )

            // 트랜잭션 리스트 (가운데, 스크롤)
            if (state.transactions.isEmpty()) {
                EmptyTransactions(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(state.transactions, key = { it.id }) { transaction ->
                        TransactionListItem(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactions(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(BankTestTags.DETAIL_TX_EMPTY),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.account_transactions_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Surface 배경은 내비게이션 바 뒤까지 채우되(edge-to-edge), 버튼 콘텐츠는 navigationBarsPadding으로
    // 시스템 내비 바 위로 띄워 겹침을 막는다.
    Surface(color = MaterialTheme.colorScheme.background) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(BankTestTags.DETAIL_SEND)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(stringResource(R.string.account_action_send))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AccountDetailScreenPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "거래내역 다건(스크롤)")
@Composable
private fun AccountDetailScreenLongListPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailStateLongList,
            onIntent = {},
        )
    }
}
