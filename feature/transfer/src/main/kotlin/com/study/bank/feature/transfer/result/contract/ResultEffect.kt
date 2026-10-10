package com.study.bank.feature.transfer.result.contract

sealed interface ResultEffect {
    data class Finish(val sourceAccountId: String) : ResultEffect
}
