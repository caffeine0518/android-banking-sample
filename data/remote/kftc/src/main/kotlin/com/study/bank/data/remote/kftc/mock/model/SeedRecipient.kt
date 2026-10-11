package com.study.bank.data.remote.kftc.mock.model

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount

internal class SeedRecipient private constructor(
    val bankCodeStd: String,
    val accountNum: AccountNumDigits,
    val accountId: String,
    val holderName: String,
    val isDormantOrClosed: Boolean,
) {

    companion object {
        fun own(account: SeedAccount): SeedRecipient = SeedRecipient(
            bankCodeStd = account.bankCodeStd,
            accountNum = AccountNumDigits.of(account.accountNum),
            accountId = account.fintechUseNum,
            holderName = account.accountHolderName,
            isDormantOrClosed = false,
        )

        fun external(
            bankCodeStd: String,
            accountNum: String,
            holderName: String,
            isDormantOrClosed: Boolean = false,
        ): SeedRecipient {
            val digits = AccountNumDigits.of(accountNum)
            return SeedRecipient(
                bankCodeStd = bankCodeStd,
                accountNum = digits,
                accountId = "ext-$bankCodeStd-${digits.value}",
                holderName = holderName,
                isDormantOrClosed = isDormantOrClosed,
            )
        }
    }
}
