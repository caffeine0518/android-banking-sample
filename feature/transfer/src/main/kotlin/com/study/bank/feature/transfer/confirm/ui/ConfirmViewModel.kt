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
import com.study.bank.feature.transfer.navigation.ARG_AMOUNT
import com.study.bank.feature.transfer.navigation.ARG_SOURCE_ACCOUNT_ID
import com.study.bank.feature.transfer.navigation.transferRecipientArg
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val confirmUiMapper: ConfirmUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val sourceAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(ARG_SOURCE_ACCOUNT_ID)) { "accountId 인자 누락" },
    )
    // 수취인·금액은 라우트로 확정돼 화면 동안 고정이다.
    private val recipient = savedStateHandle.transferRecipientArg()
    private val amount = checkNotNull(savedStateHandle.get<Long>(ARG_AMOUNT)) { "amount 인자 누락" }

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
                // 단발 가드: 첫 탭에서만 Submit. 연타해도 둘째부터는 submitting=true라 무시된다.
                if (state.detail != null && !state.submitting) {
                    setState { copy(submitting = true) }
                    sendEffect(
                        ConfirmEffect.Submit(
                            sourceAccountId = sourceAccountId.value,
                            recipient = recipient,
                            amount = amount,
                        ),
                    )
                }
            }

            // 수취인·금액은 라우트로 확정돼 고정이므로, 출금계좌가 로딩되면 확정 정보를 채운다.
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
    }
}
