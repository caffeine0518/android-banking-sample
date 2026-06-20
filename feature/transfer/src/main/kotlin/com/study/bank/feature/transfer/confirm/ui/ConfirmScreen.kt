package com.study.bank.feature.transfer.confirm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.format
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.confirm.contract.ConfirmIntent
import com.study.bank.feature.transfer.confirm.contract.ConfirmState
import com.study.bank.feature.transfer.confirm.ui.component.ConfirmInfoRow
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi
import com.study.bank.feature.transfer.confirm.ui.preview.PreviewConfirmState
import com.study.bank.feature.transfer.recipient.ui.model.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfirmScreen(
    state: ConfirmState,
    onIntent: (ConfirmIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onIntent(ConfirmIntent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.transfer_action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomBar(
                enabled = state.detail != null,
                onSend = { onIntent(ConfirmIntent.SendClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            val detail = state.detail
            Spacer(Modifier.weight(0.8f))
            if (detail != null) {
                TitleBlock(detail)
            }
            Spacer(Modifier.weight(1f))
            if (detail != null) {
                InfoSection(detail = detail, onIntent = onIntent)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColumnScope.TitleBlock(detail: ConfirmDetailUi) {
    val recipientLine = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(detail.recipientHolderName)
        }
        append(stringResource(R.string.transfer_confirm_title_to))
    }
    Text(
        text = recipientLine,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.transfer_confirm_title_amount, detail.amount.format()),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.transfer_confirm_title_question),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun InfoSection(detail: ConfirmDetailUi, onIntent: (ConfirmIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ConfirmInfoRow(
            label = stringResource(R.string.transfer_confirm_label_display_name),
            value = detail.displayName,
            onClick = { onIntent(ConfirmIntent.DisplayNameClicked) },
        )
        ConfirmInfoRow(
            label = stringResource(R.string.transfer_confirm_label_source),
            value = stringResource(
                R.string.transfer_confirm_source_format,
                detail.sourceNickname ?: detail.sourceType.label(),
            ),
            onClick = { onIntent(ConfirmIntent.SourceAccountClicked) },
        )
        ConfirmInfoRow(
            label = stringResource(R.string.transfer_confirm_label_deposit),
            value = stringResource(
                R.string.transfer_account_subtitle,
                detail.recipientBankDisplayName,
                detail.recipientNumberMasked,
            ),
            leadingIcon = Icons.Filled.CheckCircle,
        )
    }
}

@Composable
private fun BottomBar(enabled: Boolean, onSend: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Button(
                onClick = onSend,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.transfer_confirm_send))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.transfer_confirm_fee_free),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ConfirmScreenPreview() {
    MaterialTheme {
        ConfirmScreen(state = PreviewConfirmState, onIntent = {})
    }
}
