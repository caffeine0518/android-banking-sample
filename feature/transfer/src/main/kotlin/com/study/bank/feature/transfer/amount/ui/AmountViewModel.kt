package com.study.bank.feature.transfer.amount.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.transfer.amount.contract.AmountAction
import com.study.bank.feature.transfer.amount.contract.AmountEffect
import com.study.bank.feature.transfer.amount.contract.AmountInternalAction
import com.study.bank.feature.transfer.amount.contract.AmountIntent
import com.study.bank.feature.transfer.amount.contract.AmountState
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi
import com.study.bank.feature.transfer.amount.ui.model.AmountUiMapper
import com.study.bank.feature.transfer.navigation.TransferAmountRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AmountViewModel.Factory::class)
class AmountViewModel @AssistedInject constructor(
    @Assisted route: TransferAmountRoute,
    private val accountRepository: AccountRepository,
    private val amountUiMapper: AmountUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: TransferAmountRoute): AmountViewModel
    }

    private val sourceAccountId = AccountId(route.sourceAccountId)

    private val recipient = route.recipient

    private val store = MviStore<AmountState, AmountAction, AmountEffect>(
        initialState = AmountState(recipient = amountUiMapper.mapRecipient(recipient)),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            AmountIntent.BackClicked -> sendEffect(AmountEffect.NavigateBack)

            is AmountIntent.DigitAppended ->
                setState { copy(amount = appendDigit(amount, action.digit, balanceCap(source))) }

            AmountIntent.BackspacePressed -> setState { copy(amount = amount / 10) }

            AmountIntent.FillBalanceClicked -> setState { copy(amount = balanceCap(source)) }

            AmountIntent.NextClicked -> {
                if (state.isAmountEntered) {
                    sendEffect(
                        AmountEffect.NavigateNext(
                            sourceAccountId = sourceAccountId.value,
                            recipient = recipient,
                            amount = state.amount,
                        ),
                    )
                }
            }

            is AmountInternalAction.SourceUpdated -> setState {
                copy(source = action.source?.let(amountUiMapper::mapSource))
            }
        }
    }

    val state: StateFlow<AmountState> = store.state
    val effect: Flow<AmountEffect> = store.effect

    init {
        collectSource()
    }

    fun onIntent(intent: AmountIntent) {
        store.sendIntent(intent)
    }

    private fun appendDigit(current: Long, digit: String, cap: Long): Long {
        val prefix = if (current == 0L) "" else current.toString()
        val next = (prefix + digit).toLongOrNull() ?: return current
        return next.coerceAtMost(cap)
    }

    private fun balanceCap(source: AmountSourceUi?): Long {
        val balance = source?.balance ?: return Long.MAX_VALUE
        return balance.amount.movePointRight(balance.currency.exponent).longValueExact()
    }

    private fun collectSource() {
        viewModelScope.launch {
            accountRepository.observeAccount(sourceAccountId)
                .catch { error -> Log.e(TAG, "출금계좌 관찰 실패", error) }
                .collect { source -> store.sendIntent(AmountInternalAction.SourceUpdated(source)) }
        }
    }

    private companion object {
        const val TAG = "AmountViewModel"
    }
}
