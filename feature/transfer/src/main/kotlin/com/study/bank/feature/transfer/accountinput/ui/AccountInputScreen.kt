package com.study.bank.feature.transfer.accountinput.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.accountinput.contract.AccountInputError
import com.study.bank.feature.transfer.accountinput.contract.AccountInputIntent
import com.study.bank.feature.transfer.accountinput.contract.AccountInputState
import com.study.bank.feature.transfer.accountinput.ui.component.BankPickerSheet
import com.study.bank.feature.transfer.accountinput.ui.debug.AccountInputDebugBar
import com.study.bank.feature.transfer.accountinput.ui.preview.PreviewAccountInputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountInputScreen(
    state: AccountInputState,
    onIntent: (AccountInputIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onIntent(AccountInputIntent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.transfer_action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            // 입력이 시작되면 확인 버튼 노출(스크린샷 1·3과 동일). 키보드 위로 띄운다.
            if (state.accountNumber.isNotBlank()) {
                ConfirmBar(
                    enabled = state.isConfirmEnabled,
                    loading = state.isResolving,
                    onConfirm = { onIntent(AccountInputIntent.ConfirmClicked) },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.transfer_account_input_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )

            AccountNumberField(
                value = state.accountNumber,
                onValueChange = { onIntent(AccountInputIntent.AccountNumberChanged(it)) },
                onClear = { onIntent(AccountInputIntent.AccountNumberCleared) },
            )

            Spacer(Modifier.height(16.dp))

            BankSelector(
                bank = state.selectedBank,
                onClick = { onIntent(AccountInputIntent.BankSelectorClicked) },
            )

            // 디버그 빌드에서만 칩이 보인다(릴리스는 stub → Unit).
            // 일반 입력과 동일 경로로 채운다(번호는 숫자 필터를 그대로 통과).
            AccountInputDebugBar(
                onApplyPreset = { number, bank ->
                    onIntent(AccountInputIntent.AccountNumberChanged(number))
                    onIntent(AccountInputIntent.BankSelected(bank))
                },
            )

            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }

    if (state.isBankPickerVisible) {
        ModalBottomSheet(onDismissRequest = { onIntent(AccountInputIntent.BankPickerDismissed) }) {
            BankPickerSheet(
                banks = BankCode.entries,
                selected = state.selectedBank,
                onSelect = { onIntent(AccountInputIntent.BankSelected(it)) },
            )
        }
    }
}

@Composable
private fun AccountNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.transfer_account_input_number_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.transfer_account_input_clear),
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    )
}

@Composable
private fun BankSelector(bank: BankCode, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.transfer_account_input_bank_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = bank.displayName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun ConfirmBar(enabled: Boolean, loading: Boolean, onConfirm: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Button(
                onClick = onConfirm,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.transfer_account_input_confirm))
                }
            }
        }
    }
}

private fun AccountInputError.messageRes(): Int = when (this) {
    AccountInputError.NOT_FOUND -> R.string.transfer_account_input_error_not_found
    AccountInputError.INACTIVE -> R.string.transfer_account_input_error_inactive
    AccountInputError.SELF_TRANSFER -> R.string.transfer_account_input_error_self
    AccountInputError.NETWORK -> R.string.transfer_account_input_error_network
    AccountInputError.UNKNOWN -> R.string.transfer_account_input_error_unknown
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AccountInputScreenPreview() {
    MaterialTheme {
        AccountInputScreen(state = PreviewAccountInputState, onIntent = {})
    }
}
