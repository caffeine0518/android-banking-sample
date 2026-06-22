package com.study.bank

/**
 * E2E가 특정 계좌를 지목할 때 쓰는 **안정적 식별자(AccountId = fintechUseNum) 픽스처**.
 *
 * 송금 플로우는 "같은 통화 성공 / 다른 통화 거절"로 분기하므로 임의의 계좌가 아니라 통화가 정해진 특정
 * 계좌를 골라야 한다. 그 선택을 표시명("월급통장")이 아니라 변하지 않는 id로 한다 — 서버가 계좌명을
 * 바꿔도, 로케일이 달라도 테스트가 깨지지 않는다(표시 문자열이 아닌 식별 계약에만 의존).
 *
 * 값의 출처는 KftcAccountSeed이며, 여기서는 통화·역할만 주석으로 계약을 문서화한다.
 */
internal object E2eSeedAccounts {
    const val PAYROLL_KRW = "120220112345678901234001"   // 토스뱅크 KRW(출금 기본)
    const val FOREIGN_USD = "120220112345678901234002"   // 토스뱅크 USD(동일통화 출금·다통화 거절용)
    const val SAFEBOX_KRW = "120220112345678901234003"   // 토스뱅크 KRW(동일통화 수취)
    const val TWD_TRAVEL = "120220112345678901234005"    // 토스뱅크 TWD(상세 화면 검증용)
    const val FOREIGN_USD_2 = "120220112345678901234007" // 토스뱅크 USD(동일통화 수취 짝, 소수점 보존)
}
