package com.study.bank.feature.transfer.recipient.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.navigation.TransferRecipientArg
import com.study.bank.feature.transfer.navigation.TransferRecipientRoute
import com.study.bank.feature.transfer.recipient.contract.RecipientAction
import com.study.bank.feature.transfer.recipient.contract.RecipientEffect
import com.study.bank.feature.transfer.recipient.contract.RecipientInternalAction
import com.study.bank.feature.transfer.recipient.contract.RecipientIntent
import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.model.AccountUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = RecipientViewModel.Factory::class)
class RecipientViewModel @AssistedInject constructor(
    @Assisted route: TransferRecipientRoute,
    private val accountRepository: AccountRepository,
    private val accountUiMapper: AccountUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: TransferRecipientRoute): RecipientViewModel
    }

    private val sourceAccountId = AccountId(route.sourceAccountId)

    private var accountsById: Map<String, Account> = emptyMap()

    private val store = MviStore<RecipientState, RecipientAction, RecipientEffect>(
        initialState = RecipientState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            RecipientIntent.BackClicked -> sendEffect(RecipientEffect.NavigateBack)

            RecipientIntent.AccountNumberInputClicked -> {
                sendEffect(RecipientEffect.NavigateToAccountNumberInput(sourceAccountId.value))
            }

            is RecipientIntent.MyAccountClicked -> {
                val account = accountsById[action.accountId]
                if (account != null) {
                    sendEffect(
                        RecipientEffect.NavigateToAmount(
                            sourceAccountId = sourceAccountId.value,
                            recipient = TransferRecipientArg(
                                bankCode = account.bankCode.code,
                                accountNumber = account.number.value,
                                holderName = account.holderName,
                            ),
                        ),
                    )
                }
            }

            is RecipientInternalAction.MyAccountsUpdated -> {
                accountsById = action.accounts.associateBy { it.id.value }
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
