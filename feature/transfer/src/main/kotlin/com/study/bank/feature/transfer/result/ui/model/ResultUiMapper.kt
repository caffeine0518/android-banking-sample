package com.study.bank.feature.transfer.result.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.transfer.TransferOutcome
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    fun mapHeader(recipient: Account, amount: Long, currency: Currency): ResultHeaderUi =
        ResultHeaderUi(
            recipientName = recipient.holderName,
            amount = moneyUiMapper.map(Money.of(amount, currency)),
        )

    fun mapFailure(failure: TransferOutcome.Failure): ResultFailureUi = when (failure) {
        TransferOutcome.Failure.InsufficientFunds -> ResultFailureUi.INSUFFICIENT_FUNDS
        TransferOutcome.Failure.InvalidRecipient -> ResultFailureUi.INVALID_RECIPIENT
        is TransferOutcome.Failure.DailyLimitExceeded -> ResultFailureUi.LIMIT_EXCEEDED
        is TransferOutcome.Failure.PerTransactionLimitExceeded -> ResultFailureUi.LIMIT_EXCEEDED
        is TransferOutcome.Failure.Network -> ResultFailureUi.NETWORK
        is TransferOutcome.Failure.Unknown -> ResultFailureUi.UNKNOWN
    }
}
