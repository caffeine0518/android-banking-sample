package com.study.bank.feature.transfer.result.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.feature.transfer.R

/** 송금 실패 사유(도메인 TransferOutcome.Failure → 사용자 메시지). */
enum class ResultFailureUi {
    INSUFFICIENT_FUNDS,
    INVALID_RECIPIENT,
    LIMIT_EXCEEDED,
    NETWORK,
    UNKNOWN,
}

@Composable
internal fun ResultFailureUi.message(): String = stringResource(
    when (this) {
        ResultFailureUi.INSUFFICIENT_FUNDS -> R.string.transfer_result_error_insufficient
        ResultFailureUi.INVALID_RECIPIENT -> R.string.transfer_result_error_invalid_recipient
        ResultFailureUi.LIMIT_EXCEEDED -> R.string.transfer_result_error_limit
        ResultFailureUi.NETWORK -> R.string.transfer_result_error_network
        ResultFailureUi.UNKNOWN -> R.string.transfer_result_error_unknown
    },
)
