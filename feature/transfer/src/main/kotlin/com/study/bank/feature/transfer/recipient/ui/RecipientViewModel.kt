package com.study.bank.feature.transfer.recipient.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.recipient.contract.RecipientAction
import com.study.bank.feature.transfer.recipient.contract.RecipientEffect
import com.study.bank.feature.transfer.recipient.contract.RecipientInternalAction
import com.study.bank.feature.transfer.recipient.contract.RecipientIntent
import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.model.AccountUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class RecipientViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val accountUiMapper: AccountUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    // 출금계좌(보내는 쪽). "내 계좌" 목록에서 자기 자신은 제외한다.
    private val sourceAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(TRANSFER_ACCOUNT_ID_ARG)) { "accountId 인자 누락" },
    )

    private val store = MviStore<RecipientState, RecipientAction, RecipientEffect>(
        initialState = RecipientState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            RecipientIntent.BackClicked -> sendEffect(RecipientEffect.NavigateBack)

            RecipientIntent.AccountNumberInputClicked -> {
                sendEffect(RecipientEffect.NavigateToAccountNumberInput)
            }

            is RecipientIntent.MyAccountClicked -> sendEffect(
                RecipientEffect.NavigateToAmount(
                    sourceAccountId = sourceAccountId.value,
                    recipientAccountId = action.accountId,
                ),
            )

            is RecipientInternalAction.MyAccountsUpdated -> {
                setState {
                    copy(
                        myAccounts = action.accounts
                            .filterNot { it.id == sourceAccountId }
                            .map(accountUiMapper::map),
                    )
                }
            }
        }
    }

    val state: StateFlow<RecipientState> = store.state
    val effect: Flow<RecipientEffect> = store.effect

    init {
        collectMyAccounts()
    }

    fun onIntent(intent: RecipientIntent) {
        store.sendIntent(intent)
    }

    private fun collectMyAccounts() {
        viewModelScope.launch {
            accountRepository.observeAccounts()
                .catch { error -> Log.e(TAG, "Failed to observe accounts", error) }
                .collect { accounts ->
                    store.sendIntent(RecipientInternalAction.MyAccountsUpdated(accounts))
                }
        }
    }

    private companion object {
        const val TAG = "RecipientViewModel"
    }
}
