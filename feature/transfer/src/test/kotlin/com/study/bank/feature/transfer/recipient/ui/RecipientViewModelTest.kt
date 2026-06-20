package com.study.bank.feature.transfer.recipient.ui

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.recipient.contract.RecipientEffect
import com.study.bank.feature.transfer.recipient.contract.RecipientIntent
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import com.study.bank.feature.transfer.recipient.ui.model.AccountUiMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipientViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountUiMapper = AccountUiMapper()

    @Test
    fun `내 계좌 목록은 출금계좌를 제외하고 노출된다`() = runTest {
        val repo = FakeAccountRepository()
        val vm = buildViewModel(repo)

        repo.emit(account(SOURCE_ID), account("acc-2"), account("acc-3"))

        assertEquals(listOf("acc-2", "acc-3"), vm.state.value.myAccounts.map { it.id })
    }

    @Test
    fun `계좌번호 입력 버튼을 누르면 NavigateToAccountNumberInput effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(RecipientIntent.AccountNumberInputClicked)
            assertEquals(RecipientEffect.NavigateToAccountNumberInput, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BackClicked는 NavigateBack effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(RecipientIntent.BackClicked)
            assertEquals(RecipientEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `내 계좌를 선택하면 출금·수취 식별자를 실은 NavigateToAmount effect를 보낸다`() = runTest {
        val vm = buildViewModel(FakeAccountRepository())

        vm.effect.test {
            vm.onIntent(RecipientIntent.MyAccountClicked("acc-2"))
            assertEquals(
                RecipientEffect.NavigateToAmount(
                    sourceAccountId = SOURCE_ID,
                    recipientAccountId = "acc-2",
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildViewModel(repo: FakeAccountRepository) = RecipientViewModel(
        savedStateHandle = SavedStateHandle(mapOf(TRANSFER_ACCOUNT_ID_ARG to SOURCE_ID)),
        accountRepository = repo,
        accountUiMapper = accountUiMapper,
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun account(id: String) = Account(
        id = AccountId(id),
        number = AccountNumber("1000-12-3456789"),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
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

    private class TestDispatcherProvider(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }

    private companion object {
        const val SOURCE_ID = "120220112345678901234001"
    }
}
