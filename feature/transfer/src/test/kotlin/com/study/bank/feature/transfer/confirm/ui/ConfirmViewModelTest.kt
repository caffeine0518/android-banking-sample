package com.study.bank.feature.transfer.confirm.ui

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
import com.study.bank.feature.transfer.confirm.contract.ConfirmEffect
import com.study.bank.feature.transfer.confirm.contract.ConfirmIntent
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmUiMapper
import com.study.bank.feature.transfer.navigation.TransferConfirmRoute
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val confirmUiMapper = ConfirmUiMapper(MoneyUiMapper())

    @Test
    fun `출금·수취 계좌가 모두 로딩되면 확정 정보가 매핑된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo, amount = 2)

        // 수취인은 라우트로 확정돼 들어온다(아래 buildViewModel 참고). 출금계좌만 로딩하면 확정 정보가 채워진다.
        repo.emit(account(SOURCE_ID, holder = "박송금", nickname = "U드림 저축예금", balance = 284_797))

        val detail = vm.state.value.detail!!
        assertEquals("이수취", detail.recipientHolderName)
        assertEquals(BigDecimal.valueOf(2), detail.amount.amount)
        assertEquals("박송금", detail.displayName)
        assertEquals("U드림 저축예금", detail.sourceNickname)
        assertEquals("신한은행", detail.recipientBankDisplayName)
        assertEquals("110-123-456789", detail.recipientNumberMasked)
    }

    @Test
    fun `출금계좌 로딩 전에는 detail이 아직 null이다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo, amount = 2)

        // 출금계좌 미로딩. 수취인은 라우트로 있지만 출금계좌가 없으면 확정 정보를 만들 수 없다.
        assertNull(vm.state.value.detail)
    }

    @Test
    fun `로딩 전 SendClicked는 effect를 보내지 않는다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), amount = 2)

        vm.effect.test {
            vm.onIntent(ConfirmIntent.SendClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `로딩 후 SendClicked는 Submit effect를 보낸다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo, amount = 2)
        repo.emit(account(SOURCE_ID), account(RECIPIENT_ID))

        vm.effect.test {
            vm.onIntent(ConfirmIntent.SendClicked)
            assertEquals(
                ConfirmEffect.Submit(
                    sourceAccountId = SOURCE_ID,
                    recipient = TransferRecipientArg(
                        bankCode = "088",
                        accountNumber = "110-123-456789",
                        holderName = "이수취",
                    ),
                    amount = 2L,
                    idempotencyKey = IDEMPOTENCY_KEY,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SendClicked를 연타해도 Submit effect는 한 번만 나간다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo, amount = 2)
        repo.emit(account(SOURCE_ID), account(RECIPIENT_ID))

        vm.effect.test {
            vm.onIntent(ConfirmIntent.SendClicked)
            vm.onIntent(ConfirmIntent.SendClicked)
            vm.onIntent(ConfirmIntent.SendClicked)

            assertEquals(
                ConfirmEffect.Submit(
                    sourceAccountId = SOURCE_ID,
                    recipient = TransferRecipientArg(
                        bankCode = "088",
                        accountNumber = "110-123-456789",
                        holderName = "이수취",
                    ),
                    amount = 2L,
                    idempotencyKey = IDEMPOTENCY_KEY,
                ),
                awaitItem(),
            )
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(vm.state.value.submitting)
    }

    @Test
    fun `복원돼도 Submit effect의 멱등성 키가 보존된다`() = runTest {
        val repo = FakeAccountRepository()
        // 같은 SavedStateHandle로 VM 재생성 = 프로세스 death 후 Navigation이 확인 화면을 복원하는 상황.
        val savedStateHandle = SavedStateHandle()
        val newKey = { UUID.randomUUID().toString() }
        val first = buildViewModel(repo, amount = 2, savedStateHandle = savedStateHandle, newKey = newKey)
        val restored = buildViewModel(repo, amount = 2, savedStateHandle = savedStateHandle, newKey = newKey)
        repo.emit(account(SOURCE_ID), account(RECIPIENT_ID))

        val firstKey = first.effect.test {
            first.onIntent(ConfirmIntent.SendClicked)
            (awaitItem() as ConfirmEffect.Submit).idempotencyKey
        }
        val restoredKey = restored.effect.test {
            restored.onIntent(ConfirmIntent.SendClicked)
            (awaitItem() as ConfirmEffect.Submit).idempotencyKey
        }

        assertEquals(firstKey, restoredKey)
    }

    @Test
    fun `BackClicked는 NavigateBack effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository(), amount = 2)

        vm.effect.test {
            vm.onIntent(ConfirmIntent.BackClicked)
            assertEquals(ConfirmEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(
        repo: FakeAccountRepository,
        amount: Long,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        newKey: () -> String = { IDEMPOTENCY_KEY },
    ) = ConfirmViewModel(
        route = TransferConfirmRoute(
            sourceAccountId = SOURCE_ID,
            recipient = TransferRecipientArg(
                bankCode = "088",
                accountNumber = "110-123-456789",
                holderName = "이수취",
            ),
            amount = amount,
        ),
        savedStateHandle = savedStateHandle,
        accountRepository = repo,
        idempotencyKeyGenerator = newKey,
        confirmUiMapper = confirmUiMapper,
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun account(
        id: String,
        holder: String = "홍길동",
        nickname: String? = "통장 $id",
        balance: Long = 1_000_000,
        bank: BankCode = BankCode.TOSS,
        number: String = "1000-12-3456789",
    ) = Account(
        id = AccountId(id),
        number = AccountNumber(number),
        bankCode = bank,
        holderName = holder,
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
        const val IDEMPOTENCY_KEY = "idem-1"
    }
}
