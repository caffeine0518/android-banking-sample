package com.study.bank.feature.transfer.result.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.usecase.transfer.ExecuteTransferUseCase
import com.study.bank.feature.transfer.navigation.TRANSFER_ACCOUNT_ID_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_AMOUNT_ARG
import com.study.bank.feature.transfer.navigation.TRANSFER_RECIPIENT_ID_ARG
import com.study.bank.feature.transfer.result.contract.ResultAction
import com.study.bank.feature.transfer.result.contract.ResultEffect
import com.study.bank.feature.transfer.result.contract.ResultInternalAction
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.contract.ResultState
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val executeTransfer: ExecuteTransferUseCase,
    private val resultUiMapper: ResultUiMapper,
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

    private val store = MviStore<ResultState, ResultAction, ResultEffect>(
        initialState = ResultState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            ResultIntent.BackClicked, ResultIntent.ConfirmClicked -> sendEffect(ResultEffect.Finish)

            ResultIntent.ShareClicked -> sendEffect(ResultEffect.Share)

            ResultIntent.LeaveMemoClicked -> sendEffect(ResultEffect.LeaveMemo)

            ResultIntent.RetryClicked -> {
                setState { copy(phase = ResultPhase.Loading) }
                execute()
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

    /** 두 계좌를 조회해 송금 요청을 만들고 실행한 뒤, 결과를 phase로 반영한다. */
    private fun execute() {
        viewModelScope.launch {
            val source = accountRepository.findAccount(sourceAccountId)
            val recipient = accountRepository.findAccount(recipientAccountId)
            if (source == null || recipient == null) {
                Log.e(TAG, "출금/수취 계좌 조회 실패 (source=$source, recipient=$recipient)")
                store.sendIntent(
                    ResultInternalAction.Finished(ResultPhase.Failure(ResultFailureUi.UNKNOWN)),
                )
                return@launch
            }

            store.sendIntent(
                ResultInternalAction.HeaderReady(
                    resultUiMapper.mapHeader(recipient, amount, source.balance.currency),
                ),
            )

            val request = TransferRequest(
                fromAccountId = sourceAccountId,
                toAccountNumber = recipient.number,
                toBankCode = recipient.bankCode,
                amount = Money.of(amount, source.balance.currency),
                memo = null,
                idempotencyKey = UUID.randomUUID().toString(),
            )
            val outcome = runCatching { executeTransfer(request) }
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
