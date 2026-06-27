package com.study.bank.feature.account.ui

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
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
    fun `거래내역 스트림이 방출되면 state_transactions로 노출된다`() = runTest {
        val txRepo = FakeTransactionRepository()
        val vm = buildViewModel(FakeAccountRepository(), txRepo)
        val tx = transaction("tx-1", TransactionType.TRANSFER_OUT, 50_000)

        txRepo.emit(listOf(tx))

        assertEquals(listOf(transactionUiMapper.map(tx)), vm.state.value.transactions)
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
    fun `init 시 계좌와 거래내역을 각각 1회 refresh하고 완료 후 로딩이 해제된다`() = runTest {
        val accountRepo = FakeAccountRepository()
        val txRepo = FakeTransactionRepository()

        val vm = buildViewModel(accountRepo, txRepo)

        assertEquals(1, accountRepo.refreshCount)
        assertEquals(1, txRepo.refreshCount)
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
        private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
        var refreshCount: Int = 0
            private set

        fun emit(transactions: List<Transaction>) {
            transactionsFlow.value = transactions
        }

        override fun observeTransactions(accountId: AccountId): Flow<List<Transaction>> = transactionsFlow
        override suspend fun refresh(accountId: AccountId) {
            refreshCount++
        }
        // todo 페이징 스트림 UI 마이그레이션
        override fun transactionStream(accountId: AccountId): Flow<PagingData<Transaction>> = emptyFlow()
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
