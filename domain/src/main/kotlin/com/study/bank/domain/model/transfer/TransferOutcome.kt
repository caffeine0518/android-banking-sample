package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.Money

sealed interface TransferOutcome {

    data class Success(val result: TransferResult) : TransferOutcome

    sealed interface Failure : TransferOutcome {
        data object InsufficientFunds : Failure
        data object InvalidRecipient : Failure
        data class DailyLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class PerTransactionLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class Network(val cause: Throwable) : Failure
        data class Unknown(val cause: Throwable) : Failure
    }
}
