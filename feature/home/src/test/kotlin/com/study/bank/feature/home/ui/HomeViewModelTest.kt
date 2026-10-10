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
import com.study.bank.domain.usecase.account.ObserveTotalAssetsUseCase
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
        // LocaleTargetCurrency가 시스템 로케일을 따르므로 표시 통화를 KRW로 고정한다.
        Locale.setDefault(Locale.KOREA)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `총자산 스트림이 방출되면 state_totalAssets로 노출된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo) // 기본 환율은 KRW → 1

        repo.emit(account(id = "acc-1", amount = 1_000_000, currency = Currency.KRW))

        assertEquals(moneyUiMapper.map(Money.of(1_000_000, Currency.KRW)), vm.state.value.totalAssets)
    }

    @Test
    fun `AccountClicked 인텐트는 해당 accountId로 NavigateToAccountDetail effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(HomeIntent.AccountClicked("acc-42"))

            assertEquals(HomeEffect.NavigateToAccountDetail("acc-42"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init 시 Refresh가 발행돼 refresh가 1회 호출되고 완료 후 로딩이 해제된다`() = runTest {
        val repo = FakeAccountRepository()

        val vm = buildViewModel(repo)

        assertEquals(1, repo.refreshCount)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `로딩 중에는 추가 Refresh가 무시돼 refresh가 중복 호출되지 않는다`() = runTest {
        // 첫 refresh를 gate에서 대기시켜 로딩 상태를 유지한다.
        val gate = CompletableDeferred<Unit>()
        val repo = FakeAccountRepository().apply { onRefresh = { gate.await() } }
        val vm = buildViewModel(repo)

        assertTrue(vm.state.value.isLoading)
        assertEquals(1, repo.refreshCount)

        vm.onIntent(HomeIntent.Refresh)

        assertEquals("로딩 중 발행된 Refresh는 무시돼야 한다", 1, repo.refreshCount)
        gate.complete(Unit)
    }

    @Test
    fun `refresh가 실패하면 크래시 없이 계좌 스트림은 흐르고 로딩이 풀리며 ShowRefreshError가 발행된다`() = runTest {
        val repo = FakeAccountRepository().apply { onRefresh = { throw IOException("network down") } }
        val vm = buildViewModel(repo)
        val account = account(id = "acc-1", amount = 1_000_000, currency = Currency.KRW)

        repo.emit(account)

        assertEquals(listOf(accountUiMapper.map(account)), vm.state.value.accounts)
        assertFalse("실패 경로도 RefreshFinished로 로딩을 풀어야 한다", vm.state.value.isLoading)
        assertEquals(1, repo.refreshCount)

        vm.effect.test {
            assertEquals(HomeEffect.ShowRefreshError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(
        repo: FakeAccountRepository,
        fx: FakeFxRateRepository = fakeFx(Currency.KRW to BigDecimal.ONE),
    ) = HomeViewModel(
        accountRepository = repo,
        observeTotalAssets = ObserveTotalAssetsUseCase(repo, fx),
        accountUiMapper = accountUiMapper,
        moneyUiMapper = moneyUiMapper,
        localeTargetCurrency = LocaleTargetCurrency(),
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

        var onRefresh: suspend () -> Unit = {}

        var refreshCount: Int = 0
            private set

        fun emit(vararg accounts: Account) {
            accountsFlow.value = accounts.toList()
        }

        override fun observeAccounts(): Flow<List<Account>> = accountsFlow

        override fun observeAccount(id: AccountId): Flow<Account?> =
            accountsFlow.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun findAccount(id: AccountId): Account? =
            accountsFlow.value.firstOrNull { it.id == id }

        override suspend fun refresh() {
            refreshCount++
            onRefresh()
        }
    }

    private class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private class FakeFxRateRepository(
        private val rates: Map<Currency, BigDecimal>,
    ) : FxRateRepository {
        // FxRateRepository 계약: target → 1은 항상 포함된다.
        override fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>> =
            flowOf(rates + (target to (rates[target] ?: BigDecimal.ONE)))
    }
}
