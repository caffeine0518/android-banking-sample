package com.study.bank.feature.transfer.result.contract

sealed interface ResultEffect {
    /** 송금 플로우 종료(홈으로 복귀). 백/확인 공통. */
    data object Finish : ResultEffect

    /** 공유 시트. 미구현이라 현재는 placeholder. */
    data object Share : ResultEffect

    /** 메모 편집 화면. 미구현이라 현재는 placeholder. */
    data object LeaveMemo : ResultEffect
}
