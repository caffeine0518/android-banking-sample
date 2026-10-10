package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.seed.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalServiceImpl
import com.study.bank.data.remote.kftc.mock.service.WithdrawExecutor
import com.study.bank.data.remote.kftc.mock.service.WithdrawPlanner
import com.study.bank.data.remote.kftc.mock.storage.MockKftcDatabase
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.seed
import java.time.Clock
import org.robolectric.RuntimeEnvironment

/** Hilt 그래프와 같은 mock 은행 의존성을 수동으로 조립한다. 인스턴스마다 새 DB를 쓴다. */
internal class TestMockBank(
    private val accountSeed: List<SeedAccount> = KftcAccountSeed.accounts,
    clock: Clock = Clock.systemDefaultZone(),
) {
    val database: MockKftcDatabase =
        MockKftcDatabase.inMemory(RuntimeEnvironment.getApplication()).also { it.seed(accountSeed) }

    val accountDao = database.accountDao()
    val transactionDao = database.transactionDao()

    val withdrawalService = KftcWithdrawalServiceImpl(
        transactionScope = database.transactionScopeDao(),
        planner = WithdrawPlanner(accountDao),
        executor = WithdrawExecutor(accountDao, transactionDao, clock),
        withdrawalDao = database.withdrawalDao(),
    )

    fun reseed() = database.seed(accountSeed)
}
