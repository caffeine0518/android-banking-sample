package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

/**
 * KFTC mock 서버가 부팅 시 적재하는 시드 계좌.
 *
 * 외화 계좌를 함께 두어 다통화 도메인이 실제 호출 흐름 끝까지 흐르는지 통합 테스트가 검증할 수 있게 한다.
 */
internal object KftcAccountSeed {

    val accounts: List<SeedAccount> = listOf(
        SeedAccount(
            fintechUseNum = KftcSeedAccountIds.PAYROLL_KRW,
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
            fintechUseNum = KftcSeedAccountIds.FX_USD,
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
            fintechUseNum = KftcSeedAccountIds.SAFEBOX_KRW,
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
            fintechUseNum = KftcSeedAccountIds.SHINHAN_KRW,
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
            fintechUseNum = KftcSeedAccountIds.TWD_TRAVEL,
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
            fintechUseNum = KftcSeedAccountIds.VND_DONG,
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
        // '외화통장 USD'의 수취 짝 — 동일 통화 송금의 소수점 보존을 E2E로 검증한다.
        SeedAccount(
            fintechUseNum = KftcSeedAccountIds.FX_USD_2,
            bankCodeStd = "092",
            bankName = "토스뱅크",
            accountNum = "1000-98-7778889",
            accountNumMasked = "1000-98-***8889",
            accountAlias = "외화통장 USD 2",
            accountHolderName = "홍길동",
            accountType = "1",
            balanceAmt = "5000.00",
            currencyCode = "USD",
            productName = "토스뱅크 외화통장",
        ),
    )
}
