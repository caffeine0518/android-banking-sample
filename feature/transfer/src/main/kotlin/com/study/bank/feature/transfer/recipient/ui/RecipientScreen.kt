package com.study.bank.feature.transfer.recipient.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.recipient.contract.RecipientIntent
import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.component.MyAccountRow
import com.study.bank.feature.transfer.recipient.ui.preview.PreviewRecipientState
import com.study.bank.feature.transfer.recipient.ui.preview.PreviewRecipientStateLongList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipientScreen(
    state: RecipientState,
    onIntent: (RecipientIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onIntent(RecipientIntent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.transfer_action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // contentType: 타이틀/입력버튼/섹션헤더/계좌 행은 구조가 달라, 스크롤 시 같은 타입끼리만
            // composition이 재사용되도록 타입을 구분한다.
            item(contentType = "title") {
                Text(
                    text = stringResource(R.string.transfer_recipient_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(BankTestTags.SCREEN_RECIPIENT)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
            item(contentType = "account_input") {
                // 입력은 별도 화면에서 받으므로 여기선 입력 필드처럼 보이는 '버튼'(탭 → 입력 화면).
                Surface(
                    onClick = { onIntent(RecipientIntent.AccountNumberInputClicked) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.transfer_account_number_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item(contentType = "section_header") {
                Text(
                    text = stringResource(R.string.transfer_my_accounts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
            items(state.myAccounts, key = { it.id }, contentType = { "account" }) { account ->
                MyAccountRow(
                    account = account,
                    onClick = { onIntent(RecipientIntent.MyAccountClicked(account.id)) },
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipientScreenPreview() {
    MaterialTheme {
        RecipientScreen(
            state = PreviewRecipientState,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "내 계좌 다건(스크롤)")
@Composable
private fun RecipientScreenLongListPreview() {
    MaterialTheme {
        RecipientScreen(
            state = PreviewRecipientStateLongList,
            onIntent = {},
        )
    }
}
