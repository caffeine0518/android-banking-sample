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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.home.R
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.component.AccountListItem
import com.study.bank.feature.home.ui.component.TotalBalanceHeader
import com.study.bank.feature.home.ui.preview.PreviewHomeState
import com.study.bank.feature.home.ui.preview.PreviewHomeStateLongList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            // 에러는 문구가 아니라 "스낵바가 떴다"는 사실로 검증하므로 안정 태그를 부여한다.
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, modifier = Modifier.testTag(BankTestTags.HOME_SNACKBAR))
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    TextButton(
                        onClick = { onIntent(HomeIntent.Refresh) },
                        modifier = Modifier.testTag(BankTestTags.HOME_REFRESH),
                    ) {
                        Text(stringResource(R.string.home_action_refresh))
                    }
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(BankTestTags.SCREEN_HOME),
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
    onAccountClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // contentType: 헤더와 계좌 행은 구조가 달라, 스크롤 시 같은 타입끼리만 재사용되도록 구분한다.
        item(contentType = "header") {
            TotalBalanceHeader(
                totalAssets = state.totalAssets,
                unconvertedAssets = state.unconvertedAssets,
            )
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        items(state.accounts, key = { it.id }, contentType = { "account" }) { account ->
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
            state = PreviewHomeState,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "계좌 다건(스크롤)")
@Composable
private fun HomeScreenLongListPreview() {
    MaterialTheme {
        HomeScreen(
            state = PreviewHomeStateLongList,
            onIntent = {},
        )
    }
}
