package com.study.bank.feature.transfer.result.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.study.bank.feature.transfer.R

/**
 * 송금 실패 사유(도메인 TransferOutcome.Failure → 사용자 메시지).
 *
 * [isDefinite]는 서버에서 출금되지 않은 것이 확정된 실패인지를 나타낸다. NETWORK·UNKNOWN은 응답만 유실되고
 * 출금이 반영됐을 수 있으므로 false — 같은 멱등성 키로 재시도하는 경로만 허용해야 한다.
 */
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
