package com.study.bank.feature.transfer.result.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.format
import com.study.bank.core.ui.testing.BankTestTags
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.contract.ResultState
import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi
import com.study.bank.feature.transfer.result.ui.model.message
import com.study.bank.feature.transfer.result.ui.preview.PreviewResultFailureState
import com.study.bank.feature.transfer.result.ui.preview.PreviewResultLoadingState
import com.study.bank.feature.transfer.result.ui.preview.PreviewResultSuccessState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResultScreen(
    state: ResultState,
    onIntent: (ResultIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    // 송금 진행(로딩) 중에는 백 버튼을 숨겨 중도 이탈/혼동을 막는다. 성공·실패에서만 노출.
                    if (state.phase != ResultPhase.Loading) {
                        IconButton(onClick = { onIntent(ResultIntent.BackClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.transfer_action_back),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = { BottomBar(phase = state.phase, onIntent = onIntent) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (val phase = state.phase) {
                ResultPhase.Loading -> LoadingContent()
                ResultPhase.Success -> OutcomeContent(
                    icon = Icons.Filled.CheckCircle,
                    iconTint = MaterialTheme.colorScheme.primary,
                    header = state.header,
                    lastLine = stringResource(R.string.transfer_result_success_sent),
                    reason = null,
                    testTag = BankTestTags.RESULT_SUCCESS,
                    chip = {
                        MemoChip(onClick = { onIntent(ResultIntent.LeaveMemoClicked) })
                    },
                )
                is ResultPhase.Failure -> OutcomeContent(
                    icon = Icons.Filled.Warning,
                    iconTint = MaterialTheme.colorScheme.error,
                    header = state.header,
                    lastLine = stringResource(R.string.transfer_result_failure_title),
                    reason = phase.reason.message(),
                    testTag = BankTestTags.RESULT_FAILURE,
                    chip = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.transfer_result_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OutcomeContent(
    icon: ImageVector,
    iconTint: Color,
    header: ResultHeaderUi?,
    lastLine: String,
    reason: String?,
    testTag: String,
    chip: (@Composable () -> Unit)?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // 성공/실패 결과 화면을 문구가 아니라 안정 태그로 식별.
        modifier = Modifier.testTag(testTag),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(88.dp),
        )
        Spacer(Modifier.height(24.dp))
        TitleBlock(header = header, lastLine = lastLine)
        if (reason != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (chip != null) {
            Spacer(Modifier.height(24.dp))
            chip()
        }
    }
}

@Composable
private fun TitleBlock(header: ResultHeaderUi?, lastLine: String) {
    if (header != null) {
        val nameLine = buildAnnotatedString {
            append(header.recipientName)
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append(stringResource(R.string.transfer_confirm_title_to))
            }
        }
        Text(
            text = nameLine,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.transfer_confirm_title_amount, header.amount.format()),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Text(
        text = lastLine,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MemoChip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = stringResource(R.string.transfer_result_leave_memo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun BottomBar(phase: ResultPhase, onIntent: (ResultIntent) -> Unit) {
    if (phase == ResultPhase.Loading) return
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (phase) {
                is ResultPhase.Failure -> FilledTonalButton(
                    onClick = { onIntent(ResultIntent.RetryClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.transfer_result_retry))
                }
                else -> FilledTonalButton(
                    onClick = { onIntent(ResultIntent.ShareClicked) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.transfer_result_share))
                }
            }
            Button(
                onClick = { onIntent(ResultIntent.ConfirmClicked) },
                modifier = Modifier
                    .weight(1f)
                    .testTag(BankTestTags.RESULT_CONFIRM),
            ) {
                Text(stringResource(R.string.transfer_result_confirm))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ResultSuccessPreview() {
    MaterialTheme { ResultScreen(state = PreviewResultSuccessState, onIntent = {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ResultFailurePreview() {
    MaterialTheme { ResultScreen(state = PreviewResultFailureState, onIntent = {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ResultLoadingPreview() {
    MaterialTheme { ResultScreen(state = PreviewResultLoadingState, onIntent = {}) }
}
