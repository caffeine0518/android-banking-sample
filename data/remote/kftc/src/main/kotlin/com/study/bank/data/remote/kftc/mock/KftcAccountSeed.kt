package com.study.bank.data.remote.kftc.mock

/**
 * KFTC mock 서버가 부팅 시 로드하는 시드 데이터.
 *
 * 토스뱅크 3개(KRW 수시/SAVINGS + USD 외화) + 신한 KRW 1개 = 4개. 토스뱅크 외환을 시드에 포함해
 * 다통화 도메인이 실제 호출 흐름까지 끝까지 흐르는지 통합 테스트가 검증할 수 있게 한다.
 */
internal object KftcAccountSeed {

    val accounts: List<SeedAccount> = listOf(
        SeedAccount(
            fintechUseNum = "120220112345678901234001",
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNumMasked = "1000-12-***6789",
            accountAlias = "월급통장",
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "2847320",
            currencyCode = "KRW",
            productName = "토스뱅크 통장",
        ),
        SeedAccount(
            fintechUseNum = "120220112345678901234002",
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNumMasked = "1000-98-***4321",
            accountAlias = "외화통장 USD",
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "3245.80",
            currencyCode = "USD",
            productName = "토스뱅크 외화통장",
        ),
        SeedAccount(
            fintechUseNum = "120220112345678901234003",
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNumMasked = "1000-55-***4443",
            accountAlias = "세이프박스",
            accountHolderName = "홍길동",
            accountType = "2",
            balanceAmt = "12000000",
            currencyCode = "KRW",
            productName = "토스뱅크 세이프박스",
        ),
        SeedAccount(
            fintechUseNum = "120220112345678901234004",
            bankCodeStd = "088",
            bankName = "신한은행",
            accountNumMasked = "110-23-***7890",
            accountAlias = null,
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "450000",
            currencyCode = "KRW",
            productName = "신한 주거래 통장",
        ),
    )
}
