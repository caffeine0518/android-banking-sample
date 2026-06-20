package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.Money

sealed interface TransferOutcome {

    data class Success(val result: TransferResult) : TransferOutcome

    sealed interface Failure : TransferOutcome {
        data object InsufficientFunds : Failure
        data object InvalidRecipient : Failure

        /** 출금계좌와 수취계좌의 통화가 달라 거절됨(환전 송금 미지원). */
        data object CurrencyMismatch : Failure
        data class DailyLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class PerTransactionLimitExceeded(val limit: Money, val attempted: Money) : Failure
        data class Network(val cause: Throwable) : Failure
        data class Unknown(val cause: Throwable) : Failure
    }
}
