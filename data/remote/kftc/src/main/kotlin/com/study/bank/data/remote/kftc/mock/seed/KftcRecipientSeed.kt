package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.model.SeedRecipient
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

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
