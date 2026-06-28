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
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.account.R
import com.study.bank.feature.account.contract.AccountDetailIntent
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.component.AccountDetailHeader
import com.study.bank.feature.account.ui.component.TransactionListItem
import com.study.bank.feature.account.ui.model.TransactionUi
import com.study.bank.feature.account.ui.preview.PreviewAccountDetailState
import com.study.bank.feature.account.ui.preview.previewTransactions
import com.study.bank.feature.account.ui.preview.previewTransactionsEmpty
import com.study.bank.feature.account.ui.preview.previewTransactionsError
import com.study.bank.feature.account.ui.preview.previewTransactionsFooterError
import com.study.bank.feature.account.ui.preview.previewTransactionsFooterLoading
import com.study.bank.feature.account.ui.preview.previewTransactionsLong
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountDetailScreen(
    state: AccountDetailState,
    transactions: Flow<PagingData<TransactionUi>>,
    onIntent: (AccountDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val transactionItems = transactions.collectAsLazyPagingItems()
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
                    TextButton(onClick = {
                        onIntent(AccountDetailIntent.Refresh)
                        transactionItems.refresh()
                    }) {
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
            if (state.isLoading || transactionItems.loadState.refresh is LoadState.Loading) {
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

            TransactionList(
                modifier = Modifier.weight(1f),
                items = transactionItems,
            )
        }
    }
}

@Composable
private fun TransactionList(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<TransactionUi>,
) {
    if (items.itemCount == 0) {
        TransactionsPlaceholder(
            refresh = items.loadState.refresh,
            onRetry = items::retry,
            modifier = modifier,
        )
    } else {
        TransactionItems(items = items, modifier = modifier)
    }
}

@Composable
private fun TransactionsPlaceholder(
    refresh: LoadState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (refresh) {
        is LoadState.Error -> TransactionsError(onRetry = onRetry, modifier = modifier)
        is LoadState.NotLoading -> EmptyTransactions(modifier = modifier)
        is LoadState.Loading -> Unit
    }
}

/** 거래 목록(스크롤) + 다음 페이지 적재 상태 푸터([appendStateFooter]). */
@Composable
private fun TransactionItems(
    items: LazyPagingItems<TransactionUi>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 8.dp),
    ) {
        items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
            items[index]?.let { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    modifier = Modifier.testTag(BankTestTags.transactionItem(transaction.id)),
                )
            }
        }
        appendStateFooter(items)
    }
}

/** 목록 하단 푸터: 다음 페이지를 당겨오는 중이면 진행 표시, append(다음 페이지)·refresh(새로고침)가 실패하면 재시도 버튼. */
private fun LazyListScope.appendStateFooter(items: LazyPagingItems<TransactionUi>) {
    val append = items.loadState.append
    val refresh = items.loadState.refresh
    when {
        append is LoadState.Loading -> item {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag(BankTestTags.DETAIL_TX_FOOTER_LOADING),
            )
        }

        append is LoadState.Error || refresh is LoadState.Error -> item {
            FooterRetry(onRetry = items::retry)
        }

        else -> Unit
    }
}

@Composable
private fun TransactionsError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(BankTestTags.DETAIL_TX_ERROR),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.account_transactions_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag(BankTestTags.DETAIL_TX_RETRY),
            ) {
                Text(stringResource(R.string.account_transactions_retry))
            }
        }
    }
}

@Composable
private fun FooterRetry(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(BankTestTags.DETAIL_TX_FOOTER_RETRY)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.account_transactions_retry))
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
            transactions = previewTransactions,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "거래내역 다건(스크롤)")
@Composable
private fun AccountDetailScreenLongListPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            transactions = previewTransactionsLong,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "거래내역 없음")
@Composable
private fun AccountDetailScreenEmptyPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            transactions = previewTransactionsEmpty,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "거래내역 로드 실패(전체)")
@Composable
private fun AccountDetailScreenErrorPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            transactions = previewTransactionsError,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "다음 페이지 실패(푸터 재시도)")
@Composable
private fun AccountDetailScreenFooterErrorPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            transactions = previewTransactionsFooterError,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "다음 페이지 로딩(푸터 진행)")
@Composable
private fun AccountDetailScreenFooterLoadingPreview() {
    MaterialTheme {
        AccountDetailScreen(
            state = PreviewAccountDetailState,
            transactions = previewTransactionsFooterLoading,
            onIntent = {},
        )
    }
}
