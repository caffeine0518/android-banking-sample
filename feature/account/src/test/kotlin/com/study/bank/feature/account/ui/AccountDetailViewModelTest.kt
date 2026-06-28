package com.study.bank.feature.account.ui

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.testing.asSnapshot
import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.domain.model.transaction.Counterparty
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transaction.TransactionType
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.TransactionRepository
import com.study.bank.feature.account.contract.AccountDetailEffect
import com.study.bank.feature.account.contract.AccountDetailIntent
import com.study.bank.feature.account.testutil.MainDispatcherRule
import com.study.bank.feature.account.ui.model.AccountUiMapper
import com.study.bank.feature.account.ui.model.TransactionUiMapper
import com.study.bank.feature.account.ui.navigation.ACCOUNT_ID_ARG
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val moneyUiMapper = MoneyUiMapper()
    private val accountUiMapper = AccountUiMapper(moneyUiMapper)
    private val transactionUiMapper = TransactionUiMapper(moneyUiMapper)

    @Test
    fun `계좌 스트림이 방출되면 state_account로 노출된다`() = runTest {
        val accountRepo = FakeAccountRepository()
        val vm = buildViewModel(accountRepo, FakeTransactionRepository())
        val account = account(ACCOUNT_ID, 2_847_320)

        accountRepo.emitAccount(account)

        assertEquals(accountUiMapper.map(account), vm.state.value.account)
    }

    @Test
    fun `거래내역 페이징 스트림을 화면 계좌 id로 요청한다`() = runTest {
        val txRepo = FakeTransactionRepository()

        buildViewModel(FakeAccountRepository(), txRepo) // transactions val 초기화 시 transactionStream(accountId) 호출

        assertEquals(ACCOUNT_ID, txRepo.lastStreamAccountId?.value)
    }

    @Test
    fun `거래내역 페이징이 UI 모델로 매핑된다`() = runTest {
        // VM이 적용하는 변환(PagingData.map(uiMapper))을 cachedIn 없이 그대로 검증한다.
        val tx = transaction("tx-1", TransactionType.TRANSFER_OUT, 50_000)

        val snapshot = flowOf(PagingData.from(listOf(tx)))
            .map { pagingData -> pagingData.map(transactionUiMapper::map) }
            .asSnapshot()

        assertEquals(listOf(transactionUiMapper.map(tx)), snapshot)
    }

    @Test
    fun `SendClicked 인텐트는 accountId로 NavigateToTransfer effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), FakeTransactionRepository())

        vm.effect.test {
            vm.onIntent(AccountDetailIntent.SendClicked)

            assertEquals(AccountDetailEffect.NavigateToTransfer(ACCOUNT_ID), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BackClicked 인텐트는 NavigateBack effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), FakeTransactionRepository())

        vm.effect.test {
            vm.onIntent(AccountDetailIntent.BackClicked)

            assertEquals(AccountDetailEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init 시 계좌 잔액을 1회 refresh하고 완료 후 로딩이 해제된다`() = runTest {
        val accountRepo = FakeAccountRepository()
        val txRepo = FakeTransactionRepository()

        val vm = buildViewModel(accountRepo, txRepo)

        // 거래내역 refresh는 페이징(LazyPagingItems)이 소유하므로 VM은 잔액만 새로고침한다.
        assertEquals(1, accountRepo.refreshCount)
        assertEquals(0, txRepo.refreshCount)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `refresh가 실패하면 로딩이 풀리며 ShowRefreshError가 발행된다`() = runTest {
        val accountRepo = FakeAccountRepository().apply { onRefresh = { throw IOException("network down") } }
        val vm = buildViewModel(accountRepo, FakeTransactionRepository())

        assertFalse(vm.state.value.isLoading)
        vm.effect.test {
            assertEquals(AccountDetailEffect.ShowRefreshError, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- 픽스처 -----

    private fun buildViewModel(
        accountRepo: FakeAccountRepository,
        txRepo: FakeTransactionRepository,
    ) = AccountDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(ACCOUNT_ID_ARG to ACCOUNT_ID)),
        accountRepository = accountRepo,
        transactionRepository = txRepo,
        accountUiMapper = accountUiMapper,
        transactionUiMapper = transactionUiMapper,
        // Main(rule)과 동일 디스패처를 store에 주입 → reducer 루프까지 결정적으로 구동.
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun account(id: String, amount: Long) = Account(
        id = AccountId(id),
        number = AccountNumber("1000-12-3456789"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = Money.of(amount, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = "월급통장",
    )

    private fun transaction(id: String, type: TransactionType, amount: Long) = Transaction(
        id = TransactionId(id),
        accountId = AccountId(ACCOUNT_ID),
        type = type,
        amount = Money.of(amount, Currency.KRW),
        balanceAfter = Money.of(amount, Currency.KRW),
        counterparty = Counterparty("세이프박스", null, null),
        memo = null,
        occurredAt = Instant.parse("2026-06-18T01:30:00Z"),
        status = TransactionStatus.COMPLETED,
    )

    private class FakeAccountRepository : AccountRepository {
        private val accountFlow = MutableStateFlow<Account?>(null)
        var onRefresh: suspend () -> Unit = {}
        var refreshCount: Int = 0
            private set

        fun emitAccount(account: Account) {
            accountFlow.value = account
        }

        override fun observeAccounts(): Flow<List<Account>> =
            accountFlow.map { listOfNotNull(it) }
        override fun observeAccount(id: AccountId): Flow<Account?> = accountFlow
        override suspend fun findAccount(id: AccountId): Account? = accountFlow.value
        override suspend fun refresh() {
            refreshCount++
            onRefresh()
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        var refreshCount: Int = 0
            private set
        var lastStreamAccountId: AccountId? = null
            private set

        // 이 화면은 더 이상 단건 목록 경로를 쓰지 않는다(페이징으로 일원화). 계약 충족용 stub.
        override fun observeTransactions(accountId: AccountId): Flow<List<Transaction>> = emptyFlow()
        override suspend fun refresh(accountId: AccountId) {
            refreshCount++
        }
        override fun transactionStream(accountId: AccountId): Flow<PagingData<Transaction>> {
            lastStreamAccountId = accountId
            return flowOf(PagingData.empty())
        }
    }

    private class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private companion object {
        const val ACCOUNT_ID = "120220112345678901234001"
    }
}
