package com.study.bank.feature.home.ui

import app.cash.turbine.test
import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import com.study.bank.domain.usecase.account.TotalAssetsUseCase
import com.study.bank.feature.home.contract.HomeEffect
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.testutil.MainDispatcherRule
import com.study.bank.feature.home.ui.model.AccountUiMapper
import java.io.IOException
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val moneyUiMapper = MoneyUiMapper()
    private val accountUiMapper = AccountUiMapper(moneyUiMapper)
    private val originalLocale: Locale = Locale.getDefault()

    @Before
    fun setUp() {
        // LocaleTargetCurrency.resolve()가 시스템 로케일 의존 → 표시 통화를 KRW로 고정.
        Locale.setDefault(Locale.KOREA)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    // ----- 스트림 구독 → state 노출 -----

    @Test
    fun `총자산 스트림이 방출되면 state_totalAssets로 노출된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo) // 기본 fx: KRW→1(항등)이라 환산 없이 그대로 흐른다.

        repo.emit(account("acc-1", 1_000_000, Currency.KRW))

        // 환산·합산 정합성은 TotalAssetsUseCaseTest 담당. 여기선 usecase 결과가 state로 '연결'되는지만 본다.
        assertEquals(moneyUiMapper.map(Money.of(1_000_000, Currency.KRW)), vm.state.value.totalAssets)
    }

    // ----- intent → effect -----

    @Test
    fun `AccountClicked 인텐트는 해당 accountId로 NavigateToAccountDetail effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(HomeIntent.AccountClicked("acc-42"))

            assertEquals(HomeEffect.NavigateToAccountDetail("acc-42"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- refresh 생명주기 -----

    @Test
    fun `init 시 Refresh가 발행돼 refresh가 1회 호출되고 완료 후 로딩이 해제된다`() = runTest {
        val repo = FakeAccountRepository()

        val vm = buildViewModel(repo)

        assertEquals(1, repo.refreshCount)
        // 성공한 refresh도 RefreshFinished로 로딩 사이클을 닫아야 한다(시작→true→false).
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `로딩 중에는 추가 Refresh가 무시돼 refresh가 중복 호출되지 않는다`() = runTest {
        // 첫 refresh를 gate로 붙잡아 isLoading=true 상태를 유지시킨다.
        val gate = CompletableDeferred<Unit>()
        val repo = FakeAccountRepository().apply { onRefresh = { gate.await() } }
        val vm = buildViewModel(repo)

        // init Refresh가 처리돼 refresh #1이 gate에서 대기 → 로딩 중
        assertTrue(vm.state.value.isLoading)
        assertEquals(1, repo.refreshCount)

        vm.onIntent(HomeIntent.Refresh) // 로딩 중 → 가드돼야 함

        assertEquals("로딩 중 발행된 Refresh는 무시돼야 한다", 1, repo.refreshCount)
        gate.complete(Unit) // 첫 refresh를 끝내 정리
    }

    @Test
    fun `refresh가 실패하면 크래시 없이 계좌 스트림은 흐르고 로딩이 풀리며 ShowRefreshError가 발행된다`() = runTest {
        val repo = FakeAccountRepository().apply { onRefresh = { throw IOException("network down") } }
        val vm = buildViewModel(repo)
        val account = account("acc-1", 1_000_000, Currency.KRW)

        repo.emit(account)

        assertEquals(listOf(accountUiMapper.map(account)), vm.state.value.accounts)
        assertFalse("실패 경로도 RefreshFinished로 로딩을 풀어야 한다", vm.state.value.isLoading)
        assertEquals(1, repo.refreshCount)

        // 실패 경로 전용 신호: 성공 경로에서는 발행되지 않으므로 이 단언이 실패/성공을 구분한다.
        vm.effect.test {
            assertEquals(HomeEffect.ShowRefreshError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- 테스트 픽스처 -----

    private fun buildViewModel(
        repo: FakeAccountRepository,
        fx: FakeFxRateRepository = fakeFx(Currency.KRW to BigDecimal.ONE),
    ) = HomeViewModel(
        accountRepository = repo,
        totalAssetsUseCase = TotalAssetsUseCase(repo, fx),
        accountUiMapper = accountUiMapper,
        moneyUiMapper = moneyUiMapper,
        localeTargetCurrency = LocaleTargetCurrency(),
        // 핵심: Main(rule)과 동일한 디스패처를 store에 주입 → reducer 루프까지 결정적으로 구동.
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun account(id: String, amount: Long, currency: Currency, nickname: String? = null) =
        account(id, Money.of(amount, currency), nickname)

    private fun account(id: String, amount: String, currency: Currency, nickname: String? = null) =
        account(id, Money.of(amount, currency), nickname)

    private fun account(id: String, balance: Money, nickname: String? = null) = Account(
        id = AccountId(id),
        number = AccountNumber(id.replace("-", "")),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = balance,
        type = AccountType.CHECKING,
        nickname = nickname,
    )

    private fun fakeFx(vararg rates: Pair<Currency, BigDecimal>) =
        FakeFxRateRepository(rates.toMap())

    private class FakeAccountRepository : AccountRepository {

        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        /** refresh()가 실제로 할 일. 기본은 즉시 성공. 테스트가 지연/예외를 주입한다. */
        var onRefresh: suspend () -> Unit = {}

        // 모든 실행이 단일 TestDispatcher 위에서 직렬로 도므로 plain var로 충분하다.
        var refreshCount: Int = 0
            private set

        /** 계좌 스트림에 새 목록을 흘려보낸다(테스트의 'act'). */
        fun emit(vararg accounts: Account) {
            accountsFlow.value = accounts.toList()
        }

        override fun observeAccounts(): Flow<List<Account>> = accountsFlow

        override fun observeAccount(id: AccountId): Flow<Account?> =
            accountsFlow.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun findAccount(id: AccountId): Account? =
            accountsFlow.value.firstOrNull { it.id == id }

        override suspend fun refresh() {
            refreshCount++ // 시도 횟수(이후 throw 포함)를 센다
            onRefresh()
        }
    }

    /** 세 디스패처를 모두 단일 [TestDispatcher]로 돌려줘 reducer 루프·viewModelScope를 같은 스케줄러에 묶는다. */
    private class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private class FakeFxRateRepository(
        private val rates: Map<Currency, BigDecimal>,
    ) : FxRateRepository {
        // 실계약(FxRateRepository KDoc): 항등 행 target → 1 은 항상 존재한다. 실제 구현과 동일하게 보장.
        override fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>> =
            flowOf(rates + (target to (rates[target] ?: BigDecimal.ONE)))
    }
}
