package com.study.bank.feature.transfer.result.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.feature.transfer.R

enum class ResultFailureUi(
    /** 출금되지 않은 것이 확정된 실패. NETWORK·UNKNOWN은 출금이 반영됐을 수 있어 같은 키로 재시도만 허용한다. */
    val isDefinite: Boolean,
) {
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
