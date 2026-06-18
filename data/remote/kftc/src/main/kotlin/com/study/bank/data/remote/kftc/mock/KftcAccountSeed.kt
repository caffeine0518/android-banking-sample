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
            accountNum = "1000-12-3456789",
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
            accountNum = "1000-98-7654321",
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
            accountNum = "1000-55-1114443",
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
            accountNum = "110-23-1237890",
            accountNumMasked = "110-23-***7890",
            accountAlias = null,
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "450000",
            currencyCode = "KRW",
            productName = "신한 주거래 통장",
        ),
        // KEXIM API가 TWD/VND 환율을 제공하지 않아 자연스럽게 환산 불가 경로로 흐름.
        SeedAccount(
            fintechUseNum = "120220112345678901234005",
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNum = "1000-77-9993322",
            accountNumMasked = "1000-77-***3322",
            accountAlias = "대만 여행자금",
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "12500.50",
            currencyCode = "TWD",
            productName = "토스뱅크 외화통장",
        ),
        SeedAccount(
            fintechUseNum = "120220112345678901234006",
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNum = "1000-66-5551144",
            accountNumMasked = "1000-66-***1144",
            accountAlias = "베트남 동",
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "1850000",
            currencyCode = "VND",
            productName = "토스뱅크 외화통장",
        ),
    )
}
