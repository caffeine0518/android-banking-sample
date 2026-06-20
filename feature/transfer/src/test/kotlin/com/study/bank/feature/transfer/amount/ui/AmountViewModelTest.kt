package com.study.bank.feature.transfer.amount.ui

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
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.amount.contract.AmountEffect
import com.study.bank.feature.transfer.amount.contract.AmountIntent
import com.study.bank.feature.transfer.amount.ui.model.AmountUiMapper
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_RECIPIENT_ID_ARG
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AmountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val amountUiMapper = AmountUiMapper(MoneyUiMapper())

    @Test
    fun `출금·수취 계좌가 로딩되면 state에 매핑된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)

        repo.emit(
            account(SOURCE_ID, nickname = "U드림 저축예금", balance = 284_797),
            account(RECIPIENT_ID, nickname = "종합매매 계좌", bank = BankCode.SHINHAN),
        )

        val state = vm.state.value
        assertEquals("U드림 저축예금", state.source?.nickname)
        assertEquals("종합매매 계좌", state.recipient?.nickname)
        assertEquals("신한은행", state.recipient?.bankDisplayName)
    }

    @Test
    fun `숫자 키를 누르면 자리수가 쌓인다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 1_000_000), account(RECIPIENT_ID))

        vm.onIntent(AmountIntent.DigitAppended("1"))
        vm.onIntent(AmountIntent.DigitAppended("2"))
        vm.onIntent(AmountIntent.DigitAppended("3"))
        assertEquals(123L, vm.state.value.amount)

        vm.onIntent(AmountIntent.DigitAppended("00"))
        assertEquals(12_300L, vm.state.value.amount)
    }

    @Test
    fun `선행 0은 쌓이지 않는다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 1_000_000), account(RECIPIENT_ID))

        vm.onIntent(AmountIntent.DigitAppended("0"))
        vm.onIntent(AmountIntent.DigitAppended("00"))
        assertEquals(0L, vm.state.value.amount)
    }

    @Test
    fun `잔액을 초과하는 입력은 잔액으로 클램프된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 100), account(RECIPIENT_ID))

        vm.onIntent(AmountIntent.DigitAppended("9"))
        vm.onIntent(AmountIntent.DigitAppended("9"))
        vm.onIntent(AmountIntent.DigitAppended("9"))

        assertEquals(100L, vm.state.value.amount)
    }

    @Test
    fun `지우기는 마지막 자리를 삭제한다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 1_000_000), account(RECIPIENT_ID))
        vm.onIntent(AmountIntent.DigitAppended("1"))
        vm.onIntent(AmountIntent.DigitAppended("2"))
        vm.onIntent(AmountIntent.DigitAppended("3"))

        vm.onIntent(AmountIntent.BackspacePressed)

        assertEquals(12L, vm.state.value.amount)
    }

    @Test
    fun `잔액 입력을 누르면 전액이 채워진다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 284_797), account(RECIPIENT_ID))

        vm.onIntent(AmountIntent.FillBalanceClicked)

        assertEquals(284_797L, vm.state.value.amount)
    }

    @Test
    fun `금액이 0이면 NextClicked는 effect를 보내지 않는다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(AmountIntent.NextClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `금액이 있으면 NextClicked는 NavigateNext effect를 보낸다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)
        repo.emit(account(SOURCE_ID, balance = 1_000_000), account(RECIPIENT_ID))
        vm.onIntent(AmountIntent.DigitAppended("5"))

        vm.effect.test {
            vm.onIntent(AmountIntent.NextClicked)
            assertEquals(AmountEffect.NavigateNext, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BackClicked는 NavigateBack effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(AmountIntent.BackClicked)
            assertEquals(AmountEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `로딩 전이면 잔액 상한이 없어 입력이 막히지 않는다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.onIntent(AmountIntent.DigitAppended("9"))

        assertEquals(9L, vm.state.value.amount)
        assertNull(vm.state.value.source)
    }

    private fun buildViewModel(repo: FakeAccountRepository) = AmountViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                TRANSFER_ACCOUNT_ID_ARG to SOURCE_ID,
                TRANSFER_RECIPIENT_ID_ARG to RECIPIENT_ID,
            ),
        ),
        accountRepository = repo,
        amountUiMapper = amountUiMapper,
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun account(
        id: String,
        nickname: String? = "통장 $id",
        balance: Long = 1_000_000,
        bank: BankCode = BankCode.TOSS,
    ) = Account(
        id = AccountId(id),
        number = AccountNumber("1000-12-3456789"),
        bankCode = bank,
        holderName = "홍길동",
        balance = Money.of(balance, Currency.KRW),
        type = AccountType.CHECKING,
        nickname = nickname,
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
