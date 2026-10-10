package com.study.bank.data.remote.kftc.mock.seed

/**
 * 시드와 값이 일치해야 하는 테스트(kftc 단위·data-di 통합·app-e2e)만 참조한다. 격리된 단위 테스트는
 * 시드 변경에 영향받지 않도록 자체 픽스처를 쓴다.
 */
object KftcSeedAccountIds {
    const val PAYROLL_KRW = "120220112345678901234001"
    const val FX_USD = "120220112345678901234002"
    const val SAFEBOX_KRW = "120220112345678901234003"
    const val SHINHAN_KRW = "120220112345678901234004"
    const val TWD_TRAVEL = "120220112345678901234005"
    const val VND_DONG = "120220112345678901234006"
    const val FX_USD_2 = "120220112345678901234007"

    fun idsOf(currencyCode: String): List<String> =
        KftcAccountSeed.accounts.filter { it.currencyCode == currencyCode }.map { it.fintechUseNum }
}
