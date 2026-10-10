package com.study.bank.feature.transfer.result.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.feature.transfer.R

enum class ResultFailureUi(val isDefinite: Boolean) {
    INSUFFICIENT_FUNDS(isDefinite = true),
    INVALID_RECIPIENT(isDefinite = true),
    CURRENCY_MISMATCH(isDefinite = true),
    LIMIT_EXCEEDED(isDefinite = true),
    NETWORK(isDefinite = false),
    UNKNOWN(isDefinite = false),
}

@Composable
internal fun ResultFailureUi.message(): String = stringResource(
    when (this) {
        ResultFailureUi.INSUFFICIENT_FUNDS -> R.string.transfer_result_error_insufficient
        ResultFailureUi.INVALID_RECIPIENT -> R.string.transfer_result_error_invalid_recipient
        ResultFailureUi.CURRENCY_MISMATCH -> R.string.transfer_result_error_currency_mismatch
        ResultFailureUi.LIMIT_EXCEEDED -> R.string.transfer_result_error_limit
        ResultFailureUi.NETWORK -> R.string.transfer_result_error_network
        ResultFailureUi.UNKNOWN -> R.string.transfer_result_error_unknown
    },
)
