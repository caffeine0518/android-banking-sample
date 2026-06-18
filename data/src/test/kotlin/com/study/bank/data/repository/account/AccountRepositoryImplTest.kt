package com.study.bank.data.repository.account

import app.cash.turbine.test
import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.local.entity.AccountEntity
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.account.FintechAccountDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.domain.model.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

    // ----- distinctUntilChanged: Room 테이블 단위 invalidation의 중복 emit 흡수 -----

    @Test
    fun `observeAccounts는 동일 스냅샷 재방출을 거르고 실제 변경만 흘린다`() = runTest {
        val dao = EmittingAccountDao()
        val repo = buildRepo(FakeKftcApiService(initialSeeds = emptyList()), dao)
        val krwId = AccountId(ENTITY_KRW.id)
        val usdId = AccountId(ENTITY_USD.id)

        repo.observeAccounts().test {
            dao.emit(listOf(ENTITY_KRW)) // 최초 스냅샷
            val initialIds = awaitItem().map { it.id }
            assertEquals(listOf(krwId), initialIds)

            // 동일 스냅샷 재방출(테이블 쓰기만 발생)은 흡수돼야 하므로 다음 방출은 '실제 변경'이어야 한다.
            // 흡수 안 되면 [KRW]가 먼저 와서 nextIds 단언이 깨진다.
            dao.emit(listOf(ENTITY_KRW))
            dao.emit(listOf(ENTITY_KRW, ENTITY_USD))
            val nextIds = awaitItem().map { it.id }
            assertEquals(listOf(krwId, usdId), nextIds)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAccount는 다른 계좌만 바뀌면 재방출하지 않는다`() = runTest {
        val dao = EmittingAccountDao()
        val repo = buildRepo(FakeKftcApiService(initialSeeds = emptyList()), dao)

        repo.observeAccount(AccountId(ENTITY_KRW.id)).test {
            dao.emit(listOf(ENTITY_KRW)) // KRW 등장
            val initialId = awaitItem()?.id?.value
            assertEquals(ENTITY_KRW.id, initialId)

            // USD만 추가 — 테이블은 invalidate되지만 KRW는 불변이라 흡수돼야 한다.
            // 이어서 KRW 자체를 바꾸면 그 변경만 다음 방출로 와야 한다.
            dao.emit(listOf(ENTITY_KRW, ENTITY_USD))
            dao.emit(listOf(ENTITY_KRW.copy(nickname = "별칭변경"), ENTITY_USD))
            val changedNickname = awaitItem()?.nickname
            assertEquals("별칭변경", changedNickname)

            cancelAndIgnoreRemainingEvents()
        }
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

        // DAO에 직접 밀어넣을 엔티티. type은 KFTC 코드가 아닌 AccountType enum 이름이어야 한다.
        val ENTITY_KRW = AccountEntity(
            id = SEED_TOSS_KRW.fintechUseNum,
            number = SEED_TOSS_KRW.accountNumMasked,
            bankCode = SEED_TOSS_KRW.bankCode,
            holderName = SEED_TOSS_KRW.holderName,
            balanceAmount = SEED_TOSS_KRW.balanceAmt,
            balanceCurrency = SEED_TOSS_KRW.currencyCode,
            type = "CHECKING",
            nickname = SEED_TOSS_KRW.alias,
        )
        val ENTITY_USD = AccountEntity(
            id = SEED_TOSS_USD.fintechUseNum,
            number = SEED_TOSS_USD.accountNumMasked,
            bankCode = SEED_TOSS_USD.bankCode,
            holderName = SEED_TOSS_USD.holderName,
            balanceAmount = SEED_TOSS_USD.balanceAmt,
            balanceCurrency = SEED_TOSS_USD.currencyCode,
            type = "CHECKING",
            nickname = SEED_TOSS_USD.alias,
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

        // 이 테스트가 쓰지 않는 엔드포인트(KftcApiService 계약 충족용).
        override suspend fun getTransactionList(
            bankTranId: String,
            fintechUseNum: String,
            fromDate: String,
            toDate: String,
            tranDtime: String,
            inquiryType: String,
            inquiryBase: String,
            sortOrder: String,
        ): TransactionListResponse = error("이 테스트는 거래내역 엔드포인트를 쓰지 않는다")

        override suspend fun withdraw(request: WithdrawTransferRequest): WithdrawTransferResponse =
            error("이 테스트는 출금이체 엔드포인트를 쓰지 않는다")

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

    /**
     * Room InvalidationTracker는 테이블 단위라 결과가 같아도 쓰기마다 재방출한다.
     * [FakeAccountDao]의 MutableStateFlow는 자체 conflation이 있어 그 행동을 못 살리므로,
     * distinctUntilChanged 검증 전용으로 중복을 그대로 흘리는 SharedFlow 기반 페이크를 둔다.
     */
    private class EmittingAccountDao : AccountDao {

        private val source =
            MutableSharedFlow<List<AccountEntity>>(replay = 1, extraBufferCapacity = 16)

        suspend fun emit(entities: List<AccountEntity>) = source.emit(entities)

        override fun observeAll(): Flow<List<AccountEntity>> = source

        override fun observeById(id: String): Flow<AccountEntity?> =
            source.map { entities -> entities.firstOrNull { it.id == id } }

        override suspend fun findById(id: String): AccountEntity? = error("unused")
        override suspend fun count(): Int = error("unused")
        override suspend fun insertAll(entities: List<AccountEntity>) = error("unused")
        override suspend fun clear() = error("unused")
        override suspend fun replaceAll(entities: List<AccountEntity>) = error("unused")
    }
}
