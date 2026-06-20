package com.study.bank.feature.transfer.confirm.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.confirm.contract.ConfirmAction
import com.study.bank.feature.transfer.confirm.contract.ConfirmEffect
import com.study.bank.feature.transfer.confirm.contract.ConfirmInternalAction
import com.study.bank.feature.transfer.confirm.contract.ConfirmIntent
import com.study.bank.feature.transfer.confirm.contract.ConfirmState
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmUiMapper
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_AMOUNT_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_RECIPIENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val confirmUiMapper: ConfirmUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val sourceAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(TRANSFER_ACCOUNT_ID_ARG)) { "accountId 인자 누락" },
    )
    private val recipientAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(TRANSFER_RECIPIENT_ID_ARG)) { "recipientId 인자 누락" },
    )
    private val amount =
        checkNotNull(savedStateHandle.get<Long>(TRANSFER_AMOUNT_ARG)) { "amount 인자 누락" }

    private val store = MviStore<ConfirmState, ConfirmAction, ConfirmEffect>(
        initialState = ConfirmState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            ConfirmIntent.BackClicked -> sendEffect(ConfirmEffect.NavigateBack)

            ConfirmIntent.DisplayNameClicked -> sendEffect(ConfirmEffect.EditDisplayName)

            ConfirmIntent.SourceAccountClicked -> sendEffect(ConfirmEffect.ChangeSource)

            ConfirmIntent.SendClicked -> {
                if (state.detail != null) sendEffect(ConfirmEffect.Submit)
            }

            is ConfirmInternalAction.PartiesLoaded -> {
                val source = action.source
                val recipient = action.recipient
                if (source != null && recipient != null) {
                    setState { copy(detail = confirmUiMapper.map(source, recipient, amount)) }
                }
            }
        }
    }

    val state: StateFlow<ConfirmState> = store.state
    val effect: Flow<ConfirmEffect> = store.effect

    init {
        collectParties()
    }

    fun onIntent(intent: ConfirmIntent) {
        store.sendIntent(intent)
    }

    private fun collectParties() {
        viewModelScope.launch {
            combine(
                accountRepository.observeAccount(sourceAccountId),
                accountRepository.observeAccount(recipientAccountId),
            ) { source, recipient -> source to recipient }
                .catch { error -> Log.e(TAG, "Failed to observe transfer parties", error) }
                .collect { (source, recipient) ->
                    store.sendIntent(ConfirmInternalAction.PartiesLoaded(source, recipient))
                }
        }
    }

    private companion object {
        const val TAG = "ConfirmViewModel"
    }
}
