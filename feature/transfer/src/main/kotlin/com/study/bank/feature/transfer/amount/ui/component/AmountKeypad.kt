package com.study.bank.feature.transfer.amount.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.bank.feature.transfer.R

private const val BACKSPACE_GLYPH = "⌫"

/** 토스식 숫자 키패드: 1~9 / 00 / 0 / 지우기. */
@Composable
internal fun AmountKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
        KeypadRow {
            DigitKey("1", onDigit)
            DigitKey("2", onDigit)
            DigitKey("3", onDigit)
        }
        KeypadRow {
            DigitKey("4", onDigit)
            DigitKey("5", onDigit)
            DigitKey("6", onDigit)
        }
        KeypadRow {
            DigitKey("7", onDigit)
            DigitKey("8", onDigit)
            DigitKey("9", onDigit)
        }
        KeypadRow {
            DigitKey("00", onDigit)
            DigitKey("0", onDigit)
            BackspaceKey(onBackspace)
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), content = content)
}

@Composable
private fun RowScope.DigitKey(digit: String, onDigit: (String) -> Unit) {
    KeypadCell(onClick = { onDigit(digit) }) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun RowScope.BackspaceKey(onBackspace: () -> Unit) {
    val description = stringResource(R.string.transfer_amount_backspace)
    KeypadCell(
        onClick = onBackspace,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Text(text = BACKSPACE_GLYPH, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun RowScope.KeypadCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .weight(1f)
            .height(60.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
