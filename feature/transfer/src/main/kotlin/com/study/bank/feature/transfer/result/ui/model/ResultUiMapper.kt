package com.study.bank.feature.transfer.result.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.transfer.TransferOutcome
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    fun mapHeader(recipientName: String, amount: Long, currency: Currency): ResultHeaderUi =
        ResultHeaderUi(
            recipientName = recipientName,
            amount = moneyUiMapper.map(Money.ofMinor(amount, currency)),
        )

    fun mapFailure(failure: TransferOutcome.Failure): ResultFailureUi = when (failure) {
        TransferOutcome.Failure.InsufficientFunds -> ResultFailureUi.INSUFFICIENT_FUNDS
        TransferOutcome.Failure.InvalidRecipient -> ResultFailureUi.INVALID_RECIPIENT
        TransferOutcome.Failure.CurrencyMismatch -> ResultFailureUi.CURRENCY_MISMATCH
        is TransferOutcome.Failure.DailyLimitExceeded -> ResultFailureUi.LIMIT_EXCEEDED
        is TransferOutcome.Failure.PerTransactionLimitExceeded -> ResultFailureUi.LIMIT_EXCEEDED
        is TransferOutcome.Failure.Network -> ResultFailureUi.NETWORK
        is TransferOutcome.Failure.Unknown -> ResultFailureUi.UNKNOWN
    }
}
