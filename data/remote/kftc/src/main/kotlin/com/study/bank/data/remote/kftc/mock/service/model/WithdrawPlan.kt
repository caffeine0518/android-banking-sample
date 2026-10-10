package com.study.bank.data.remote.kftc.mock.service.model

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import java.math.BigDecimal

internal sealed interface WithdrawPlan {

    data class Reject(val result: WithdrawResult) : WithdrawPlan

    /** [recipient]가 null이면 외부 이체다. */
    data class Approved(
        val source: SeedAccount,
        val amount: BigDecimal,
        val recipient: SeedAccount?,
    ) : WithdrawPlan
}
