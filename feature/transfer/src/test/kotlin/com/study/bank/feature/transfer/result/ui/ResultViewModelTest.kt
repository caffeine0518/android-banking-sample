package com.study.bank.feature.transfer.result.ui

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.model.transfer.TransferResult
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.TransferRepository
import com.study.bank.domain.usecase.transfer.ExecuteTransferUseCase
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_AMOUNT_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_RECIPIENT_ID_ARG
import com.study.bank.feature.transfer.result.contract.ResultEffect
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultUiMapper
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val resultUiMapper = ResultUiMapper(MoneyUiMapper())

    @Test
    fun `송금 성공이면 header가 채워지고 phase는 Success가 된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID, holder = "강남규"), account(RECIPIENT_ID, holder = "안성재"))
        }
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        val state = vm.state.value
        assertEquals(ResultPhase.Success, state.phase)
        assertEquals("안성재", state.header?.recipientName)
        assertEquals(BigDecimal.ONE, state.header?.amount?.amount)
    }

    @Test
    fun `잔액 부족 실패면 phase는 Failure(INSUFFICIENT_FUNDS)가 된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(
            accounts,
            FakeTransferRepository(TransferOutcome.Failure.InsufficientFunds),
            amount = 1,
        )

        assertEquals(
            ResultPhase.Failure(ResultFailureUi.INSUFFICIENT_FUNDS),
            vm.state.value.phase,
        )
    }

    @Test
    fun `통화 불일치 실패면 phase는 Failure(CURRENCY_MISMATCH)가 된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(
            accounts,
            FakeTransferRepository(TransferOutcome.Failure.CurrencyMismatch),
            amount = 1,
        )

        assertEquals(
            ResultPhase.Failure(ResultFailureUi.CURRENCY_MISMATCH),
            vm.state.value.phase,
        )
    }

    @Test
    fun `실행 중 예외는 UNKNOWN 실패로 매핑된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(accounts, ThrowingTransferRepository(), amount = 1)

        assertEquals(ResultPhase.Failure(ResultFailureUi.UNKNOWN), vm.state.value.phase)
    }

    @Test
    fun `계좌를 찾지 못하면 UNKNOWN 실패가 된다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), FakeTransferRepository(success()), amount = 1)

        assertEquals(ResultPhase.Failure(ResultFailureUi.UNKNOWN), vm.state.value.phase)
    }

    @Test
    fun `다시 시도하면 실패 후 성공으로 전환된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        // 첫 실행은 네트워크 실패, 두 번째(재시도)는 성공.
        val transfer = SequencedTransferRepository(
            TransferOutcome.Failure.Network(RuntimeException("net")),
            success(),
        )
        val vm = buildViewModel(accounts, transfer, amount = 1)
        assertTrue(vm.state.value.phase is ResultPhase.Failure)

        vm.onIntent(ResultIntent.RetryClicked)

        assertEquals(ResultPhase.Success, vm.state.value.phase)
    }

    @Test
    fun `재시도해도 멱등성 키는 동일하게 유지된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        // 1차는 네트워크 실패(타임아웃 가정), 재시도는 성공.
        val transfer = SequencedTransferRepository(
            TransferOutcome.Failure.Network(RuntimeException("timeout")),
            success(),
        )
        val vm = buildViewModel(accounts, transfer, amount = 1)

        vm.onIntent(ResultIntent.RetryClicked)

        // 같은 논리적 송금이므로 재시도는 첫 시도와 동일한 키로 나가야 한다(서버 dedup → 이중출금 방지).
        assertEquals(2, transfer.requests.size)
        assertEquals(transfer.requests[0].idempotencyKey, transfer.requests[1].idempotencyKey)
    }

    @Test
    fun `복원 후 자동 재실행돼도 멱등성 키가 보존된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val savedStateHandle = SavedStateHandle(
            mapOf(
                TRANSFER_ACCOUNT_ID_ARG to SOURCE_ID,
                TRANSFER_RECIPIENT_ID_ARG to RECIPIENT_ID,
                TRANSFER_AMOUNT_ARG to 1L,
            ),
        )
        val first = SequencedTransferRepository(TransferOutcome.Failure.Network(RuntimeException("timeout")))
        buildViewModel(accounts, first, amount = 1, savedStateHandle = savedStateHandle)

        // 같은 SavedStateHandle로 VM 재생성 = 시스템 OOM kill 후 Navigation이 결과 화면을
        // 복원해 init이 송금을 자동 재실행하는 상황. 키는 그대로여야 한다.
        val second = SequencedTransferRepository(success())
        buildViewModel(accounts, second, amount = 1, savedStateHandle = savedStateHandle)

        assertEquals(
            first.requests.single().idempotencyKey,
            second.requests.single().idempotencyKey,
        )
    }

    @Test
    fun `확인은 Finish effect를 보낸다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        vm.effect.test {
            vm.onIntent(ResultIntent.ConfirmClicked)
            assertEquals(ResultEffect.Finish, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `공유하기·메모는 각각 Share·LeaveMemo effect를 보낸다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        vm.effect.test {
            vm.onIntent(ResultIntent.ShareClicked)
            assertEquals(ResultEffect.Share, awaitItem())
            vm.onIntent(ResultIntent.LeaveMemoClicked)
            assertEquals(ResultEffect.LeaveMemo, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(
        accounts: FakeAccountRepository,
        transfer: TransferRepository,
        amount: Long,
        savedStateHandle: SavedStateHandle = SavedStateHandle(
            mapOf(
                TRANSFER_ACCOUNT_ID_ARG to SOURCE_ID,
                TRANSFER_RECIPIENT_ID_ARG to RECIPIENT_ID,
                TRANSFER_AMOUNT_ARG to amount,
            ),
        ),
    ) = ResultViewModel(
        savedStateHandle = savedStateHandle,
        accountRepository = accounts,
        executeTransfer = ExecuteTransferUseCase(transfer),
        resultUiMapper = resultUiMapper,
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun success() = TransferOutcome.Success(
        TransferResult(
            transactionId = TransactionId("tx-1"),
            status = TransactionStatus.COMPLETED,
            balanceAfter = Money.of(999, Currency.KRW),
            completedAt = Instant.EPOCH,
        ),
    )

    private fun account(id: String, holder: String = "홍길동") = Account(
        id = AccountId(id),
        number = AccountNumber("1000-12-3456789"),
        bankCode = BankCode.TOSS,
        holderName = holder,
        balance = Money.of(1_000_000, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = "통장 $id",
    )

    private class FakeAccountRepository : AccountRepository {
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        fun emit(vararg accounts: Account) {
            accountsFlow.value = accounts.toList()
        }

        override fun observeAccounts(): Flow<List<Account>> = accountsFlow
        override fun observeAccount(id: AccountId): Flow<Account?> =
            accountsFlow.map { list -> list.firstOrNull { it.id == id } }
        override suspend fun findAccount(id: AccountId): Account? =
            accountsFlow.value.firstOrNull { it.id == id }
        override suspend fun refresh() = Unit
    }

    private class FakeTransferRepository(private val outcome: TransferOutcome) : TransferRepository {
        override suspend fun execute(request: TransferRequest): TransferOutcome = outcome
    }

    private class ThrowingTransferRepository : TransferRepository {
        override suspend fun execute(request: TransferRequest): TransferOutcome =
            throw RuntimeException("boom")
    }

    private class SequencedTransferRepository(
        private vararg val outcomes: TransferOutcome,
    ) : TransferRepository {
        private var index = 0
        val requests = mutableListOf<TransferRequest>()
        override suspend fun execute(request: TransferRequest): TransferOutcome {
            requests += request
            return outcomes[index++.coerceAtMost(outcomes.lastIndex)]
        }
    }

    private class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private companion object {
        const val SOURCE_ID = "source-1"
        const val RECIPIENT_ID = "recipient-1"
    }
}
