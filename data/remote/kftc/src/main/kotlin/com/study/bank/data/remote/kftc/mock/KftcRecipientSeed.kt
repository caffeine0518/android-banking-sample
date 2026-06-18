package com.study.bank.data.remote.kftc.mock

/**
 * 계좌실명조회가 조회하는 수취 계좌 디렉터리.
 *
 * 본인 명의 계좌는 [directory]에서 **계좌 시드로부터 파생**하므로(자기/내부 이체 — 계좌목록의 어떤 계좌로도
 * 송금 가능), 여기엔 계좌목록에서 유도할 수 없는 **외부 수취인만** 둔다. 본인 계좌의 accountId는 그 계좌의
 * fintech_use_num이라 SelfTransfer/내부이체가 식별되고, 외부 수취인은 합성 id를 갖는다.
 */
internal object KftcRecipientSeed {

    private val externalRecipients: List<SeedRecipient> = listOf(
        // 외부 활성 수취인(타행, 타인 명의).
        SeedRecipient(
            bankCodeStd = "088",
            accountNum = "110-555-667788",
            accountId = "ext-088-110555667788",
            holderName = "김토스",
            active = true,
        ),
        // 외부 휴면/해지 계좌 → Inactive.
        SeedRecipient(
            bankCodeStd = "004",
            accountNum = "004-999-888777",
            accountId = "ext-004-999888777",
            holderName = "이휴면",
            active = false,
        ),
    )

    /** 실명조회로 조회 가능한 전체 디렉터리 = 본인 계좌(계좌 시드 전체) + 외부 수취인. */
    fun directory(ownAccounts: List<SeedAccount>): List<SeedRecipient> =
        ownAccounts.map { it.toRecipient() } + externalRecipients

    // 본인 계좌는 활성으로 간주하고, 식별자로 그 계좌의 fintech_use_num을 그대로 쓴다.
    private fun SeedAccount.toRecipient(): SeedRecipient = SeedRecipient(
        bankCodeStd = bankCodeStd,
        accountNum = accountNum,
        accountId = fintechUseNum,
        holderName = accountHolderName,
        active = true,
    )
}
