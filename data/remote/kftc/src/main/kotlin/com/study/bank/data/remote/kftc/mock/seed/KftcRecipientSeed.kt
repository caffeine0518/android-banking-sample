package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.storage.SeedAccount

/**
 * 계좌실명조회가 조회하는 수취 계좌 디렉터리.
 *
 * 계좌목록의 어떤 계좌로도 송금할 수 있어야 하므로 본인 계좌는 [directory]가 계좌 시드에서 파생한다.
 * 여기엔 계좌목록에서 유도할 수 없는 외부 수취인만 둔다.
 */
internal object KftcRecipientSeed {

    private val externalRecipients: List<SeedRecipient> = listOf(
        SeedRecipient(
            bankCodeStd = "088",
            accountNum = "110-555-667788",
            accountId = "ext-088-110555667788",
            holderName = "김토스",
            active = true,
        ),
        SeedRecipient(
            bankCodeStd = "004",
            accountNum = "004-999-888777",
            accountId = "ext-004-999888777",
            holderName = "이휴면",
            active = false,
        ),
    )

    fun directory(ownAccounts: List<SeedAccount>): List<SeedRecipient> =
        ownAccounts.map { it.toRecipient() } + externalRecipients

    private fun SeedAccount.toRecipient(): SeedRecipient = SeedRecipient(
        bankCodeStd = bankCodeStd,
        accountNum = accountNum,
        accountId = fintechUseNum,
        holderName = accountHolderName,
        active = true,
    )
}
