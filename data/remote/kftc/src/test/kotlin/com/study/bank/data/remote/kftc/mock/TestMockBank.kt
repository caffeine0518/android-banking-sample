package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.seed.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.storage.MockKftcDatabase
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.seed
import java.time.Clock
import org.robolectric.RuntimeEnvironment

/**
 * 테스트용 mock 은행 한 벌 — Hilt가 조립하는 것과 같은 의존성을 수동으로 묶는다.
 *
 * 인스턴스마다 새 인메모리 DB를 만들어 적재하므로 테스트끼리 상태가 섞이지 않는다.
 * Robolectric 러너 안에서만 생성할 수 있다(Room이 Context를 요구).
 */
internal class TestMockBank(
    private val accountSeed: List<SeedAccount> = KftcAccountSeed.accounts,
    clock: Clock = Clock.systemDefaultZone(),
) {
    val database: MockKftcDatabase =
        MockKftcDatabase.inMemory(RuntimeEnvironment.getApplication()).also { it.seed(accountSeed) }

    val accountDao = database.accountDao()
    val transactionDao = database.transactionDao()

    val withdrawalService = KftcWithdrawalService(
        transactionScope = database.transactionScopeDao(),
        accountDao = accountDao,
        transactionDao = transactionDao,
        withdrawalDao = database.withdrawalDao(),
        clock = clock,
    )

    /** 시드 초깃값으로 되돌린다. 프로덕션에선 DB 프로바이더가 부팅 시 한 번 수행하는 그 적재다. */
    fun reseed() = database.seed(accountSeed)
}
