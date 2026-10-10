package com.study.bank.feature.transfer.result.ui

import app.cash.turbine.test
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
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import com.study.bank.feature.transfer.navigation.TransferResultRoute
import com.study.bank.feature.transfer.result.contract.ResultEffect
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultUiMapper
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
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
            emit(account(SOURCE_ID, holder = "박송금"), account(RECIPIENT_ID, holder = "이수취"))
        }
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        val state = vm.state.value
        assertEquals(ResultPhase.Success, state.phase)
        assertEquals("이수취", state.header?.recipientName)
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
    fun `출금계좌를 찾지 못하면 UNKNOWN 실패가 된다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), FakeTransferRepository(success()), amount = 1)

        assertEquals(ResultPhase.Failure(ResultFailureUi.UNKNOWN), vm.state.value.phase)
    }

    @Test
    fun `출금계좌 조회 중 예외가 발생하면 UNKNOWN 실패가 된다`() = runTest {
        val accounts = FakeAccountRepository(findError = IllegalStateException("db"))
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        assertEquals(ResultPhase.Failure(ResultFailureUi.UNKNOWN), vm.state.value.phase)
    }

    @Test
    fun `외부 수취인은 출금계좌 저장소에 없어도 라우트 신원으로 송금된다`() = runTest {
        val accounts = FakeAccountRepository().apply { emit(account(SOURCE_ID, holder = "박송금")) }
        val transfer = SequencedTransferRepository(success())
        val vm = buildViewModel(
            accounts,
            transfer,
            amount = 1,
            route = TransferResultRoute(
                sourceAccountId = SOURCE_ID,
                recipient = TransferRecipientArg(
                    bankCode = "088",
                    accountNumber = "110-555-667788",
                    holderName = "김토스",
                ),
                amount = 1L,
                idempotencyKey = IDEMPOTENCY_KEY,
            ),
        )

        assertEquals(ResultPhase.Success, vm.state.value.phase)
        assertEquals("김토스", vm.state.value.header?.recipientName)
        val request = transfer.requests.single()
        assertEquals("110-555-667788", request.toAccountNumber.value)
        assertEquals(BankCode.SHINHAN, request.toBankCode)
        assertEquals("김토스", request.recipientName)
    }

    @Test
    fun `다시 시도하면 실패 후 성공으로 전환된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
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
        val transfer = SequencedTransferRepository(
            TransferOutcome.Failure.Network(RuntimeException("timeout")),
            success(),
        )
        val vm = buildViewModel(accounts, transfer, amount = 1)

        vm.onIntent(ResultIntent.RetryClicked)

        assertEquals(2, transfer.requests.size)
        assertEquals(transfer.requests[0].idempotencyKey, transfer.requests[1].idempotencyKey)
    }

    @Test
    fun `확인 화면의 송금 한 건으로 결과 화면이 두 번 생성돼도 같은 멱등성 키로 송금한다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val first = SequencedTransferRepository(success())
        buildViewModel(accounts, first, amount = 1)
        val second = SequencedTransferRepository(success())
        buildViewModel(accounts, second, amount = 1)

        assertEquals(
            first.requests.single().idempotencyKey,
            second.requests.single().idempotencyKey,
        )
    }

    @Test
    fun `재실행 중에는 RetryClicked 연타가 무시된다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val transfer = GatedTransferRepository(retryOutcome = success())
        val vm = buildViewModel(accounts, transfer, amount = 1)
        assertTrue(vm.state.value.phase is ResultPhase.Failure)
        assertEquals(1, transfer.callCount)

        vm.onIntent(ResultIntent.RetryClicked)
        vm.onIntent(ResultIntent.RetryClicked)
        vm.onIntent(ResultIntent.RetryClicked)
        assertEquals(2, transfer.callCount)

        transfer.release()
        assertEquals(ResultPhase.Success, vm.state.value.phase)
        assertEquals(2, transfer.callCount)
    }

    @Test
    fun `확인은 출금계좌로 복귀하는 Finish effect를 보낸다`() = runTest {
        val accounts = FakeAccountRepository().apply {
            emit(account(SOURCE_ID), account(RECIPIENT_ID))
        }
        val vm = buildViewModel(accounts, FakeTransferRepository(success()), amount = 1)

        vm.effect.test {
            vm.onIntent(ResultIntent.ConfirmClicked)
            assertEquals(ResultEffect.Finish(SOURCE_ID), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(
        accounts: FakeAccountRepository,
        transfer: TransferRepository,
        amount: Long,
        route: TransferResultRoute = TransferResultRoute(
            sourceAccountId = SOURCE_ID,
            recipient = TransferRecipientArg(
                bankCode = "088",
                accountNumber = "110-123-456789",
                holderName = "이수취",
            ),
            amount = amount,
            idempotencyKey = IDEMPOTENCY_KEY,
        ),
    ) = ResultViewModel(
        route = route,
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

    private class FakeAccountRepository(
        private val findError: Throwable? = null,
    ) : AccountRepository {
        private val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

        fun emit(vararg accounts: Account) {
            accountsFlow.value = accounts.toList()
        }

        override fun observeAccounts(): Flow<List<Account>> = accountsFlow
        override fun observeAccount(id: AccountId): Flow<Account?> =
            accountsFlow.map { list -> list.firstOrNull { it.id == id } }
        override suspend fun findAccount(id: AccountId): Account? {
            findError?.let { throw it }
            return accountsFlow.value.firstOrNull { it.id == id }
        }
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
        private val _requests = mutableListOf<TransferRequest>()
        val requests: List<TransferRequest> get() = _requests
        override suspend fun execute(request: TransferRequest): TransferOutcome {
            _requests += request
            return outcomes[index++.coerceAtMost(outcomes.lastIndex)]
        }
    }

    /** 첫 호출(init)은 즉시 실패하고, 이후 호출은 [release] 전까지 멈춘다. */
    private class GatedTransferRepository(
        private val retryOutcome: TransferOutcome,
    ) : TransferRepository {
        private val gate = CompletableDeferred<Unit>()
        var callCount = 0
            private set

        override suspend fun execute(request: TransferRequest): TransferOutcome {
            callCount++
            if (callCount == 1) {
                return TransferOutcome.Failure.Network(RuntimeException("net"))
            }
            gate.await()
            return retryOutcome
        }

        fun release() {
            gate.complete(Unit)
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
        const val IDEMPOTENCY_KEY = "idem-1"
    }
}
