package com.study.bank.data.remote.kftc.mock.seed

/**
 * 시드 계좌 식별자(fintechUseNum = AccountId.value)의 단일 출처.
 *
 * 시드와 값이 일치해야 하는 테스트(kftc 단위·data-di 통합·app-e2e)만 여기를 참조한다. 격리 단위테스트는
 * 일부러 자급 픽스처를 쓴다 — 시드가 바뀌어도 영향받지 않아야 하고, feature는 이 모듈을 의존하지도 않는다.
 */
object KftcSeedAccountIds {
    const val PAYROLL_KRW = "120220112345678901234001"
    const val FX_USD = "120220112345678901234002"
    const val SAFEBOX_KRW = "120220112345678901234003"
    const val SHINHAN_KRW = "120220112345678901234004"
    const val TWD_TRAVEL = "120220112345678901234005"
    const val VND_DONG = "120220112345678901234006"
    const val FX_USD_2 = "120220112345678901234007"

    /** [currencyCode] 통화의 시드 계좌 id를 등록 순서대로. */
    fun idsOf(currencyCode: String): List<String> =
        KftcAccountSeed.accounts.filter { it.currencyCode == currencyCode }.map { it.fintechUseNum }
}
