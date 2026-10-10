package com.study.bank.data.remote.kftc.mock.service.model

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import java.math.BigDecimal

internal sealed interface WithdrawPlan {

    data class Reject(val result: WithdrawResult) : WithdrawPlan

    data class Approved(
        val source: SeedAccount,
        val amount: BigDecimal,
        val internalRecipient: SeedAccount?,
    ) : WithdrawPlan
}
