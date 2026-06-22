package com.study.bank.feature.transfer.amount.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_RECIPIENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class AmountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val amountUiMapper: AmountUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val sourceAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(TRANSFER_ACCOUNT_ID_ARG)) { "accountId 인자 누락" },
    )
    private val recipientAccountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(TRANSFER_RECIPIENT_ID_ARG)) { "recipientId 인자 누락" },
    )

    private val store = MviStore<AmountState, AmountAction, AmountEffect>(
        initialState = AmountState(),
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
                            recipientAccountId = recipientAccountId.value,
                            amount = state.amount,
                        ),
                    )
                }
            }

            is AmountInternalAction.PartiesLoaded -> setState {
                copy(
                    source = action.source?.let(amountUiMapper::mapSource),
                    recipient = action.recipient?.let(amountUiMapper::mapRecipient),
                )
            }
        }
    }

    val state: StateFlow<AmountState> = store.state
    val effect: Flow<AmountEffect> = store.effect

    init {
        collectParties()
    }

    fun onIntent(intent: AmountIntent) {
        store.sendIntent(intent)
    }

    /** 자리 추가 후 잔액 상한으로 클램프. 오버플로(자릿수 초과)는 무시한다. */
    private fun appendDigit(current: Long, digit: String, cap: Long): Long {
        val prefix = if (current == 0L) "" else current.toString()
        val next = (prefix + digit).toLongOrNull() ?: return current
        return next.coerceAtMost(cap)
    }

    /**
     * 출금계좌 잔액을 통화 최소단위(minor unit) 정수로 환산한 입력 상한.
     * 예) USD 3,245.80 → 324580센트. `toLong()`으로 절삭하면 외화 소수점이 사라지므로
     * exponent만큼 소수점을 밀어 손실 없이 환산한다. 계좌 로딩 전이면 상한 없음.
     */
    private fun balanceCap(source: AmountSourceUi?): Long {
        val balance = source?.balance ?: return Long.MAX_VALUE
        return balance.amount.movePointRight(balance.currency.exponent).longValueExact()
    }

    private fun collectParties() {
        viewModelScope.launch {
            combine(
                accountRepository.observeAccount(sourceAccountId),
                accountRepository.observeAccount(recipientAccountId),
            ) { source, recipient -> source to recipient }
                .catch { error -> Log.e(TAG, "Failed to observe transfer parties", error) }
                .collect { (source, recipient) ->
                    store.sendIntent(AmountInternalAction.PartiesLoaded(source, recipient))
                }
        }
    }

    private companion object {
        const val TAG = "AmountViewModel"
    }
}
