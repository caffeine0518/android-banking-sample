package com.study.bank.feature.transfer.result.contract

sealed interface ResultEffect {
    /**
     * 송금 플로우 종료. 백/확인 공통이며, 송금 플로우 전체를 제거하고 출금계좌 상세로 복귀해
     * 갱신된 잔액·거래내역을 보게 한다. [sourceAccountId]=복귀할 출금계좌.
     */
    data class Finish(val sourceAccountId: String) : ResultEffect
}
