package com.study.bank.feature.transfer.accountinput.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientValidation
import com.study.bank.domain.usecase.transfer.ValidateRecipientUseCase
import com.study.bank.feature.transfer.accountinput.contract.AccountInputAction
import com.study.bank.feature.transfer.accountinput.contract.AccountInputEffect
import com.study.bank.feature.transfer.accountinput.contract.AccountInputError
import com.study.bank.feature.transfer.accountinput.contract.AccountInputInternalAction
import com.study.bank.feature.transfer.accountinput.contract.AccountInputIntent
import com.study.bank.feature.transfer.accountinput.contract.AccountInputState
import com.study.bank.feature.transfer.navigation.ARG_SOURCE_ACCOUNT_ID
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AccountInputViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val validateRecipient: ValidateRecipientUseCase,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    // 출금계좌(보내는 쪽). 실명조회 시 자기이체 판별에 쓴다.
    private val sourceAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(ARG_SOURCE_ACCOUNT_ID)) { "accountId 인자 누락" },
    )

    private val store = MviStore<AccountInputState, AccountInputAction, AccountInputEffect>(
        initialState = AccountInputState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            AccountInputIntent.BackClicked -> sendEffect(AccountInputEffect.NavigateBack)

            is AccountInputIntent.AccountNumberChanged ->
                setState { copy(accountNumber = action.value.filter(Char::isDigit), error = null) }

            AccountInputIntent.AccountNumberCleared ->
                setState { copy(accountNumber = "", error = null) }

            AccountInputIntent.BankSelectorClicked -> setState { copy(isBankPickerVisible = true) }

            AccountInputIntent.BankPickerDismissed -> setState { copy(isBankPickerVisible = false) }

            is AccountInputIntent.BankSelected -> setState {
                copy(selectedBank = action.bankCode, isBankPickerVisible = false, error = null)
            }

            AccountInputIntent.ConfirmClicked -> {
                if (state.isConfirmEnabled) {
                    setState { copy(isResolving = true, error = null) }
                    resolve(state.accountNumber, state.selectedBank)
                }
            }

            is AccountInputInternalAction.Resolved -> when (val validation = action.validation) {
                is RecipientValidation.Valid -> sendEffect(
                    AccountInputEffect.NavigateToAmount(
                        sourceAccountId = sourceAccountId.value,
                        // 실명조회로 확정된 수취인 신원: 사용자가 입력한 번호·은행 + 조회된 예금주명.
                        recipient = TransferRecipientArg(
                            bankCode = state.selectedBank.code,
                            accountNumber = state.accountNumber,
                            holderName = validation.holderName,
                        ),
                    ),
                )

                RecipientValidation.NotFound ->
                    setState { copy(isResolving = false, error = AccountInputError.NOT_FOUND) }

                RecipientValidation.Inactive ->
                    setState { copy(isResolving = false, error = AccountInputError.INACTIVE) }

                RecipientValidation.SelfTransfer ->
                    setState { copy(isResolving = false, error = AccountInputError.SELF_TRANSFER) }
            }

            AccountInputInternalAction.ResolveFailed ->
                setState { copy(isResolving = false, error = AccountInputError.NETWORK) }
        }
    }

    val state: StateFlow<AccountInputState> = store.state
    val effect: Flow<AccountInputEffect> = store.effect

    fun onIntent(intent: AccountInputIntent) {
        store.sendIntent(intent)
    }

    /** (계좌번호, 은행)으로 실명조회를 돌려 결과를 내부 액션으로 되돌린다. 입력값은 호출 시점 스냅샷. */
    private fun resolve(accountNumber: String, bank: BankCode) {
        viewModelScope.launch {
            runCatching {
                validateRecipient(
                    fromAccountId = sourceAccountId,
                    toAccountNumber = AccountNumber(accountNumber),
                    toBankCode = bank,
                )
            }
                .onSuccess { store.sendIntent(AccountInputInternalAction.Resolved(it)) }
                .onFailure { error ->
                    Log.e(TAG, "수취인 실명조회 실패", error)
                    store.sendIntent(AccountInputInternalAction.ResolveFailed)
                }
        }
    }

    private companion object {
        const val TAG = "AccountInputViewModel"
    }
}
