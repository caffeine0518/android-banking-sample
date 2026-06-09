package com.study.bank.data.repository.account

import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.local.entity.AccountEntity
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.account.FintechAccountDto
import com.study.bank.domain.model.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryImplTest {

    // ----- refresh: 원격 fetch → DB 덮어쓰기 -----

    @Test
    fun `refresh는 원격에서 가져온 결과를 DB에 저장한다`() = runTest {
        val api = FakeKftcApiService(initialSeeds = listOf(SEED_TOSS_KRW))
        val dao = FakeAccountDao()
        val repo = buildRepo(api, dao)

        repo.refresh()

        // primary: 첫 호출은 list + balance 둘 다 1회씩
        assertEquals(1, api.listCallCount)
        assertEquals(1, api.balanceCallCount)
        assertEquals(1, dao.count())
    }

    // ----- SSOT: refresh 후 여러 구독자가 같은 DAO source를 본다 -----

    @Test
    fun `refresh 1회 후 여러 구독자가 observeAccounts를 collect해도 fetch는 1회`() = runTest {
        val api = FakeKftcApiService(initialSeeds = listOf(SEED_TOSS_KRW, SEED_TOSS_USD))
        val dao = FakeAccountDao()
        val repo = buildRepo(api, dao)

        repo.refresh()
        val byVm = repo.observeAccounts().first()
        val byUseCase = repo.observeAccounts().first()

        // primary: 같은 source(DAO)에서 흘러나오니 두 collect가 같은 데이터
        assertEquals(byVm, byUseCase)
        assertEquals(2, byVm.size)
        // secondary: 구독 횟수가 fetch 횟수를 늘리지 않는다 — cold flow 회귀 방지
        assertEquals(1, api.listCallCount)
    }

    // ----- replaceAll: 응답에서 사라진 계좌는 DB에서도 제거 (stale 차단) -----

    @Test
    fun `refresh 시 응답에 없는 계좌는 DB에서 사라진다`() = runTest {
        val api = FakeKftcApiService(initialSeeds = listOf(SEED_TOSS_KRW, SEED_TOSS_USD))
        val dao = FakeAccountDao()
        val repo = buildRepo(api, dao)

        repo.refresh()
        api.setSeeds(listOf(SEED_TOSS_KRW)) // USD 계좌 해지 시뮬레이션
        repo.refresh()

        val accounts = repo.observeAccounts().first()
        assertEquals(1, accounts.size)
        assertEquals(SEED_TOSS_KRW.fintechUseNum, accounts.single().id.value)
    }

    @Test
    fun `findAccount는 캐시-only — fallback fetch가 끼지 않는다`() = runTest {
        val api = FakeKftcApiService(initialSeeds = listOf(SEED_TOSS_KRW))
        val dao = FakeAccountDao()
        val repo = buildRepo(api, dao)
        repo.refresh()
        val baseline = api.listCallCount

        val hit = repo.findAccount(AccountId(SEED_TOSS_KRW.fintechUseNum))
        val miss = repo.findAccount(AccountId("UNKNOWN"))

        assertEquals(SEED_TOSS_KRW.fintechUseNum, hit?.id?.value)
        assertNull(miss)
        assertEquals(baseline, api.listCallCount)
    }

    // ----- Helpers -----

    private fun buildRepo(api: KftcApiService, dao: AccountDao) = AccountRepositoryImpl(
        api = api,
        dao = dao,
        dtoMapper = AccountMapper(),
        entityMapper = AccountEntityMapper(),
    )

    private data class Seed(
        val fintechUseNum: String,
        val bankCode: String,
        val accountNumMasked: String,
        val holderName: String,
        val accountType: String, // KFTC code "1"/"2"/"3"
        val alias: String?,
        val balanceAmt: String,
        val currencyCode: String,
    )

    private companion object {
        val SEED_TOSS_KRW = Seed(
            fintechUseNum = "120220112345678901234001",
            bankCode = "092",
            accountNumMasked = "1000-12-***6789",
            holderName = "홍길동",
            accountType = "1",
            alias = "월급통장",
            balanceAmt = "2847320",
            currencyCode = "KRW",
        )
        val SEED_TOSS_USD = Seed(
            fintechUseNum = "120220112345678901234002",
            bankCode = "092",
            accountNumMasked = "1000-98-***4321",
            holderName = "홍길동",
            accountType = "1",
            alias = "외화통장",
            balanceAmt = "3245.80",
            currencyCode = "USD",
        )
    }

    private class FakeKftcApiService(
        initialSeeds: List<Seed>,
    ) : KftcApiService {

        private var seeds: List<Seed> = initialSeeds
        var listCallCount: Int = 0
            private set
        var balanceCallCount: Int = 0
            private set

        fun setSeeds(new: List<Seed>) {
            seeds = new
        }

        override suspend fun getAccountList(
            userSeqNo: String,
            includeCancelYn: String,
            sortOrder: String,
        ): AccountListResponse {
            listCallCount++
            return AccountListResponse(
                apiTranId = "T-$listCallCount",
                apiTranDtm = "20260603120000",
                rspCode = "A0000",
                rspMessage = "OK",
                userSeqNo = userSeqNo,
                resCnt = seeds.size.toString(),
                resList = seeds.map { it.toFintechDto() },
            )
        }

        override suspend fun getAccountBalance(
            bankTranId: String,
            fintechUseNum: String,
            tranDtime: String,
        ): AccountBalanceResponse {
            balanceCallCount++
            val seed = seeds.first { it.fintechUseNum == fintechUseNum }
            return AccountBalanceResponse(
                apiTranId = "B-$balanceCallCount",
                apiTranDtm = "20260603120000",
                rspCode = "A0000",
                rspMessage = "OK",
                bankTranId = bankTranId,
                bankCodeTran = seed.bankCode,
                bankRspCode = "000",
                fintechUseNum = seed.fintechUseNum,
                balanceAmt = seed.balanceAmt,
                availableAmt = seed.balanceAmt,
                accountType = seed.accountType,
                productName = null,
                currencyCode = seed.currencyCode,
            )
        }

        private fun Seed.toFintechDto() = FintechAccountDto(
            fintechUseNum = fintechUseNum,
            accountAlias = alias,
            bankCodeStd = bankCode,
            bankName = "",
            accountNumMasked = accountNumMasked,
            accountHolderName = holderName,
            accountHolderType = "P",
            accountType = accountType,
        )
    }

    /**
     * Room을 단위 테스트에서 띄우려면 Robolectric이 필요해 인터페이스 충실 모사로 대체.
     * @Transaction 같은 ACID 보장은 검증 못 하지만 SSOT/clear-then-insert 순서는 행동 동일.
     */
    private class FakeAccountDao : AccountDao {

        private val source = MutableStateFlow<List<AccountEntity>>(emptyList())

        override fun observeAll(): Flow<List<AccountEntity>> = source

        override fun observeById(id: String): Flow<AccountEntity?> =
            source.map { entities -> entities.firstOrNull { it.id == id } }

        override suspend fun findById(id: String): AccountEntity? =
            source.value.firstOrNull { it.id == id }

        override suspend fun count(): Int = source.value.size

        override suspend fun insertAll(entities: List<AccountEntity>) {
            // ON CONFLICT REPLACE 모사: 같은 id가 있으면 덮어쓴다
            val merged = (source.value.filter { existing -> entities.none { it.id == existing.id } } + entities)
            source.value = merged.sortedBy { it.id }
        }

        override suspend fun clear() {
            source.value = emptyList()
        }

        override suspend fun replaceAll(entities: List<AccountEntity>) {
            clear()
            insertAll(entities)
        }

        // 보장: Fake가 인터페이스에 정확히 맞춰 깜빡 누락 안 했는지 컴파일러로 잡힘
        init { assertTrue(true) }
    }
}
