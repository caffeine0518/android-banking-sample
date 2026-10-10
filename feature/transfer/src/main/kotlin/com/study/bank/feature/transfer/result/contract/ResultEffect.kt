package com.study.bank.feature.transfer.result.contract

sealed interface ResultEffect {
    /** 송금 플로우 전체를 제거하고 출금계좌 상세로 복귀한다. */
    data class Finish(val sourceAccountId: String) : ResultEffect
}
