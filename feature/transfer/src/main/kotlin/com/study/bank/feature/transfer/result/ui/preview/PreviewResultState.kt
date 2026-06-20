package com.study.bank.feature.transfer.result.ui.preview

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.contract.ResultState
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi
import java.math.BigDecimal

private val previewHeader = ResultHeaderUi(
    recipientName = "링구 (이*린)",
    amount = MoneyUi(BigDecimal.ONE, CurrencyUi.KRW),
)

internal val PreviewResultSuccessState = ResultState(
    header = previewHeader,
    phase = ResultPhase.Success,
)

internal val PreviewResultFailureState = ResultState(
    header = previewHeader,
    phase = ResultPhase.Failure(ResultFailureUi.INSUFFICIENT_FUNDS),
)

internal val PreviewResultLoadingState = ResultState(
    header = null,
    phase = ResultPhase.Loading,
)
