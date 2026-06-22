package com.study.bank.data.remote.kftc.mock

/**
 * 시드 계좌의 정규 식별자(fintechUseNum = AccountId.value)의 **단일 출처**.
 *
 * [KftcAccountSeed]가 이 값으로 시드를 만들고, 시드와 반드시 일치해야 하는 모든 소비처(kftc 단위테스트·
 * data-di 통합 테스트·app-e2e)가 이 한 곳을 참조한다. 시드 id가 바뀌면 여기만 고치면 전부 따라온다 —
 * 여러 곳에 하드코딩하던 리터럴 중복을 제거한다.
 *
 * 반대로 시드와 무관한 격리 단위테스트(레포 매핑·feature ViewModel)는 임의의 샘플 id를 쓰는 자급 픽스처라
 * 일부러 이 출처에 묶지 않는다(시드 변경에 깨지지 않아야 하고, feature는 이 mock 모듈을 의존하지도 않는다).
 */
object KftcSeedAccountIds {
    const val PAYROLL_KRW = "120220112345678901234001"  // 토스뱅크 월급통장 KRW
    const val FX_USD = "120220112345678901234002"       // 토스뱅크 외화통장 USD
    const val SAFEBOX_KRW = "120220112345678901234003"  // 토스뱅크 세이프박스 KRW
    const val SHINHAN_KRW = "120220112345678901234004"  // 신한 주거래 KRW
    const val TWD_TRAVEL = "120220112345678901234005"   // 토스뱅크 대만 여행자금 TWD
    const val VND_DONG = "120220112345678901234006"     // 토스뱅크 베트남 동 VND
    const val FX_USD_2 = "120220112345678901234007"     // 토스뱅크 외화통장 USD 2(동일통화 수취 짝)

    /**
     * [currencyCode] 통화의 시드 계좌 id를 시드 등록 순서대로 반환.
     *
     * 통화 정보를 [KftcAccountSeed]에서 파생하므로(별도 하드코딩 없음), "특정 id"가 아니라 "통화 기준"으로
     * 계좌를 고르려는 소비처(app-e2e)가 의도를 표현할 수 있다 — 시드가 바뀌어도 통화 관계가 유지된다.
     */
    fun idsOf(currencyCode: String): List<String> =
        KftcAccountSeed.accounts.filter { it.currencyCode == currencyCode }.map { it.fintechUseNum }
}
