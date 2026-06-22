package com.study.bank.feature.transfer.amount.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.core.ui.model.format
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.amount.contract.AmountIntent
import com.study.bank.feature.transfer.amount.contract.AmountState
import com.study.bank.feature.transfer.amount.ui.component.AmountKeypad
import com.study.bank.feature.transfer.amount.ui.component.TransferPartyHeader
import com.study.bank.feature.transfer.amount.ui.preview.PreviewAmountState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AmountScreen(
    state: AmountState,
    onIntent: (AmountIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onIntent(AmountIntent.BackClicked) }) {
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
                showNext = state.isAmountEntered,
                onNext = { onIntent(AmountIntent.NextClicked) },
                onDigit = { onIntent(AmountIntent.DigitAppended(it)) },
                onBackspace = { onIntent(AmountIntent.BackspacePressed) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp),
        ) {
            TransferPartyHeader(source = state.source, recipient = state.recipient)
            Spacer(Modifier.height(28.dp))
            AmountValue(
                state = state,
                onFillBalance = { onIntent(AmountIntent.FillBalanceClicked) },
            )
        }
    }
}

@Composable
private fun AmountValue(state: AmountState, onFillBalance: () -> Unit) {
    val source = state.source
    val entered = state.isAmountEntered && source != null
    val amountText = if (entered) {
        // state.amount는 통화 최소단위(minor unit) 정수 → exponent만큼 소수점을 밀어 표시 금액 복원.
        val currency = source!!.balance.currency
        val displayAmount = BigDecimal.valueOf(state.amount).movePointLeft(currency.exponent)
        MoneyUi(displayAmount, currency).format()
    } else {
        stringResource(R.string.transfer_amount_hint)
    }
    Text(
        text = amountText,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = if (entered) FontWeight.Bold else FontWeight.Normal,
        color = if (entered) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(horizontal = 20.dp),
    )

    if (!entered && source != null) {
        Spacer(Modifier.height(16.dp))
        FillBalanceChip(balance = source.balance.format(), onClick = onFillBalance)
    }
}

@Composable
private fun FillBalanceChip(balance: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.transfer_amount_fill_balance, balance),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun BottomBar(
    showNext: Boolean,
    onNext: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    // 키패드 배경은 내비게이션 바 뒤까지 채우되, 콘텐츠는 navigationBarsPadding으로 시스템 바 위로 띄운다.
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            if (showNext) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(stringResource(R.string.transfer_amount_next))
                }
            }
            AmountKeypad(onDigit = onDigit, onBackspace = onBackspace)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AmountScreenPreview() {
    MaterialTheme {
        AmountScreen(state = PreviewAmountState, onIntent = {})
    }
}
