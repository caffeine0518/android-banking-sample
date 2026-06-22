package com.study.bank.feature.transfer.accountinput.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup
import com.study.bank.domain.repository.RecipientRepository
import com.study.bank.domain.usecase.transfer.ValidateRecipientUseCase
import com.study.bank.feature.transfer.accountinput.contract.AccountInputEffect
import com.study.bank.feature.transfer.accountinput.contract.AccountInputError
import com.study.bank.feature.transfer.accountinput.contract.AccountInputIntent
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import com.study.bank.feature.transfer.testutil.MainDispatcherRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountInputViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `계좌번호는 숫자만 상태에 반영된다`() = runTest {
        val vm = buildViewModel(RecipientLookup.NotFound)

        vm.onIntent(AccountInputIntent.AccountNumberChanged("86-83a69"))

        assertEquals("868369", vm.state.value.accountNumber)
    }

    @Test
    fun `지우기를 누르면 계좌번호가 비워진다`() = runTest {
        val vm = buildViewModel(RecipientLookup.NotFound)
        vm.onIntent(AccountInputIntent.AccountNumberChanged("868369666"))

        vm.onIntent(AccountInputIntent.AccountNumberCleared)

        assertEquals("", vm.state.value.accountNumber)
    }

    @Test
    fun `은행을 선택하면 선택 은행이 갱신되고 시트가 닫힌다`() = runTest {
        val vm = buildViewModel(RecipientLookup.NotFound)
        vm.onIntent(AccountInputIntent.BankSelectorClicked)

        vm.onIntent(AccountInputIntent.BankSelected(BankCode.SHINHAN))

        assertEquals(BankCode.SHINHAN, vm.state.value.selectedBank)
        assertFalse(vm.state.value.isBankPickerVisible)
    }

    @Test
    fun `BackClicked는 NavigateBack effect를 보낸다`() = runTest {
        val vm = buildViewModel(RecipientLookup.NotFound)

        vm.effect.test {
            vm.onIntent(AccountInputIntent.BackClicked)
            assertEquals(AccountInputEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `유효한 계좌면 확인 시 실명조회로 확정된 수취인 신원을 실은 NavigateToAmount effect를 보낸다`() = runTest {
        val vm = buildViewModel(RecipientLookup.Active(AccountId("acc-2"), "김토스"))
        vm.onIntent(AccountInputIntent.AccountNumberChanged("868369666"))

        vm.effect.test {
            vm.onIntent(AccountInputIntent.ConfirmClicked)
            assertEquals(
                AccountInputEffect.NavigateToAmount(
                    sourceAccountId = SOURCE_ID,
                    // 입력한 번호·기본 은행(KAKAO=090) + 조회된 예금주명. 합성 식별자가 아니라 실제 신원이 흐른다.
                    recipient = TransferRecipientArg(
                        bankCode = "090",
                        accountNumber = "868369666",
                        holderName = "김토스",
                    ),
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `없는 계좌면 확인 시 NOT_FOUND 오류를 노출한다`() = runTest {
        val vm = buildViewModel(RecipientLookup.NotFound)
        vm.onIntent(AccountInputIntent.AccountNumberChanged("868369666"))

        vm.onIntent(AccountInputIntent.ConfirmClicked)

        assertEquals(AccountInputError.NOT_FOUND, vm.state.value.error)
        assertFalse(vm.state.value.isResolving)
    }

    @Test
    fun `휴면 계좌면 확인 시 INACTIVE 오류를 노출한다`() = runTest {
        val vm = buildViewModel(RecipientLookup.Inactive(AccountId("ext-1"), "이휴면"))
        vm.onIntent(AccountInputIntent.AccountNumberChanged("868369666"))

        vm.onIntent(AccountInputIntent.ConfirmClicked)

        assertEquals(AccountInputError.INACTIVE, vm.state.value.error)
    }

    @Test
    fun `본인 계좌면 확인 시 SELF_TRANSFER 오류를 노출한다`() = runTest {
        // lookup이 출금계좌와 동일한 식별자를 돌려주면 자기이체.
        val vm = buildViewModel(RecipientLookup.Active(AccountId(SOURCE_ID), "홍길동"))
        vm.onIntent(AccountInputIntent.AccountNumberChanged("868369666"))

        vm.onIntent(AccountInputIntent.ConfirmClicked)

        assertEquals(AccountInputError.SELF_TRANSFER, vm.state.value.error)
    }

    @Test
    fun `계좌번호가 비어 있으면 확인해도 아무 일도 일어나지 않는다`() = runTest {
        val vm = buildViewModel(RecipientLookup.Active(AccountId("acc-2"), "김토스"))

        vm.onIntent(AccountInputIntent.ConfirmClicked)

        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isResolving)
    }

    private fun buildViewModel(lookup: RecipientLookup) = AccountInputViewModel(
        savedStateHandle = SavedStateHandle(mapOf("sourceAccountId" to SOURCE_ID)),
        validateRecipient = ValidateRecipientUseCase(FakeRecipientRepository(lookup)),
        dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private class FakeRecipientRepository(private val lookup: RecipientLookup) : RecipientRepository {
        override suspend fun lookup(accountNumber: AccountNumber, bankCode: BankCode): RecipientLookup =
            lookup
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
