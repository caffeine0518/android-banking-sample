package com.study.bank.feature.transfer.result.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.coroutine.cancellableCatching
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.usecase.transfer.ExecuteTransferUseCase
import com.study.bank.feature.transfer.navigation.TransferResultRoute
import com.study.bank.feature.transfer.result.contract.ResultAction
import com.study.bank.feature.transfer.result.contract.ResultEffect
import com.study.bank.feature.transfer.result.contract.ResultInternalAction
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.contract.ResultState
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultUiMapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ResultViewModel.Factory::class)
class ResultViewModel @AssistedInject constructor(
    @Assisted route: TransferResultRoute,
    private val accountRepository: AccountRepository,
    private val executeTransfer: ExecuteTransferUseCase,
    private val resultUiMapper: ResultUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(route: TransferResultRoute): ResultViewModel
    }

    private val sourceAccountId = AccountId(route.sourceAccountId)
    private val recipient = route.recipient
    private val amount = route.amount

    private val idempotencyKey = route.idempotencyKey

    private val store = MviStore<ResultState, ResultAction, ResultEffect>(
        initialState = ResultState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            ResultIntent.BackClicked, ResultIntent.ConfirmClicked ->
                sendEffect(ResultEffect.Finish(sourceAccountId.value))

            ResultIntent.RetryClicked -> {
                if (state.phase != ResultPhase.Loading) {
                    setState { copy(phase = ResultPhase.Loading) }
                    execute()
                }
            }

            is ResultInternalAction.HeaderReady -> setState { copy(header = action.header) }

            is ResultInternalAction.Finished -> setState { copy(phase = action.phase) }
        }
    }

    val state: StateFlow<ResultState> = store.state
    val effect: Flow<ResultEffect> = store.effect

    init {
        execute()
    }

    fun onIntent(intent: ResultIntent) {
        store.sendIntent(intent)
    }

    private fun execute() {
        viewModelScope.launch {
            val source = cancellableCatching { accountRepository.findAccount(sourceAccountId) }
                .onFailure { Log.e(TAG, "출금계좌 조회 중 예외", it) }
                .getOrNull()
            val toBankCode = BankCode.byCode(recipient.bankCode)
            if (source == null || toBankCode == null) {
                Log.e(TAG, "출금계좌/수취 은행 조회 실패 (source=${source?.id?.value}, bank=${recipient.bankCode})")
                store.sendIntent(
                    ResultInternalAction.Finished(ResultPhase.Failure(ResultFailureUi.UNKNOWN)),
                )
                return@launch
            }

            store.sendIntent(
                ResultInternalAction.HeaderReady(
                    resultUiMapper.mapHeader(recipient.holderName, amount, source.balance.currency),
                ),
            )

            val request = TransferRequest(
                fromAccountId = sourceAccountId,
                senderName = source.holderName,
                toAccountNumber = AccountNumber(recipient.accountNumber),
                toBankCode = toBankCode,
                recipientName = recipient.holderName,
                amount = Money.ofMinor(amount, source.balance.currency),
                memo = null,
                idempotencyKey = idempotencyKey,
            )
            val outcome = cancellableCatching { executeTransfer(request) }
                .getOrElse { error ->
                    Log.e(TAG, "송금 실행 중 예외", error)
                    TransferOutcome.Failure.Unknown(error)
                }
            store.sendIntent(ResultInternalAction.Finished(outcome.toPhase()))
        }
    }

    private fun TransferOutcome.toPhase(): ResultPhase = when (this) {
        is TransferOutcome.Success -> ResultPhase.Success
        is TransferOutcome.Failure -> ResultPhase.Failure(resultUiMapper.mapFailure(this))
    }

    private companion object {
        const val TAG = "ResultViewModel"
    }
}
