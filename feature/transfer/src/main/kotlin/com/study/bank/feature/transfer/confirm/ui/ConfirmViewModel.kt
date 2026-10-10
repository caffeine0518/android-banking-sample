package com.study.bank.feature.transfer.confirm.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transfer.IdempotencyKeyGenerator
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.confirm.contract.ConfirmAction
import com.study.bank.feature.transfer.confirm.contract.ConfirmEffect
import com.study.bank.feature.transfer.confirm.contract.ConfirmInternalAction
import com.study.bank.feature.transfer.confirm.contract.ConfirmIntent
import com.study.bank.feature.transfer.confirm.contract.ConfirmState
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmUiMapper
import com.study.bank.feature.transfer.navigation.TransferConfirmRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ConfirmViewModel.Factory::class)
class ConfirmViewModel @AssistedInject constructor(
    @Assisted route: TransferConfirmRoute,
    // 멱등성 키를 프로세스 종료 후에도 유지하려고 SavedStateHandle에 둔다.
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    idempotencyKeyGenerator: IdempotencyKeyGenerator,
    private val confirmUiMapper: ConfirmUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: TransferConfirmRoute): ConfirmViewModel
    }

    private val sourceAccountId = AccountId(route.sourceAccountId)
    private val recipient = route.recipient
    private val amount = route.amount

    /** 송금 한 건에 묶이도록 이 화면에서 한 번 발급한다. 결과 화면이 중복 생성되거나 복원돼도 같은 키로 송금한다. */
    private val idempotencyKey: String =
        savedStateHandle.get<String>(IDEMPOTENCY_KEY) ?: idempotencyKeyGenerator.newKey().also {
            savedStateHandle[IDEMPOTENCY_KEY] = it
        }

    private val store = MviStore<ConfirmState, ConfirmAction, ConfirmEffect>(
        initialState = ConfirmState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            ConfirmIntent.BackClicked -> sendEffect(ConfirmEffect.NavigateBack)

            ConfirmIntent.SendClicked -> {
                if (state.detail != null && !state.submitting) {
                    setState { copy(submitting = true) }
                    sendEffect(
                        ConfirmEffect.Submit(
                            sourceAccountId = sourceAccountId.value,
                            recipient = recipient,
                            amount = amount,
                            idempotencyKey = idempotencyKey,
                        ),
                    )
                }
            }

            is ConfirmInternalAction.SourceUpdated -> {
                val source = action.source
                if (source != null) {
                    setState { copy(detail = confirmUiMapper.map(source, recipient, amount)) }
                }
            }
        }
    }

    val state: StateFlow<ConfirmState> = store.state
    val effect: Flow<ConfirmEffect> = store.effect

    init {
        collectSource()
    }

    fun onIntent(intent: ConfirmIntent) {
        store.sendIntent(intent)
    }

    private fun collectSource() {
        viewModelScope.launch {
            accountRepository.observeAccount(sourceAccountId)
                .catch { error -> Log.e(TAG, "출금계좌 관찰 실패", error) }
                .collect { source -> store.sendIntent(ConfirmInternalAction.SourceUpdated(source)) }
        }
    }

    private companion object {
        const val TAG = "ConfirmViewModel"
        const val IDEMPOTENCY_KEY = "transfer_idempotency_key"
    }
}
