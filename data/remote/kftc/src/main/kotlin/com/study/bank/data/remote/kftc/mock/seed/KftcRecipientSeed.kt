package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.model.SeedRecipient
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

internal object KftcRecipientSeed {

    private val externalRecipients: List<SeedRecipient> = listOf(
        SeedRecipient.external(
            bankCodeStd = "088",
            accountNum = "110-555-667788",
            holderName = "김토스",
        ),
        SeedRecipient.external(
            bankCodeStd = "004",
            accountNum = "004-999-888777",
            holderName = "이휴면",
            isDormantOrClosed = true,
        ),
    )

    fun directory(ownAccounts: List<SeedAccount>): List<SeedRecipient> =
        ownAccounts.map(SeedRecipient::own) + externalRecipients
}
