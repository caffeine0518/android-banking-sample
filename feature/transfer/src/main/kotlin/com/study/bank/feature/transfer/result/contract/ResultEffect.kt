package com.study.bank.feature.transfer.result.contract

sealed interface ResultEffect {
    /**
     * 송금 플로우 종료. 백/확인 공통이며, 송금 플로우 전체를 걷어내고 출금계좌 상세로 복귀해
     * 갱신된 잔액·거래내역을 보게 한다. [sourceAccountId]=복귀할 출금계좌.
     */
    data class Finish(val sourceAccountId: String) : ResultEffect

    /** 공유 시트. 미구현이라 현재는 placeholder. */
    data object Share : ResultEffect

    /** 메모 편집 화면. 미구현이라 현재는 placeholder. */
    data object LeaveMemo : ResultEffect
}
