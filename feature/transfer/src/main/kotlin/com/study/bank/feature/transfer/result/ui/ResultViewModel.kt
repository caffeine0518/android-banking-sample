package com.study.bank.feature.transfer.result.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import com.study.bank.feature.transfer.navigation.ARG_AMOUNT
import com.study.bank.feature.transfer.navigation.ARG_SOURCE_ACCOUNT_ID
import com.study.bank.feature.transfer.navigation.transferRecipientArg
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
        checkNotNull(savedStateHandle.get<String>(ARG_SOURCE_ACCOUNT_ID)) { "accountId 인자 누락" },
    )
    // 수취인은 라우트 신원(외부 계좌는 출금계좌 저장소에 없으므로 식별자 재조회 없이 그대로 송금에 쓴다).
    private val recipient = savedStateHandle.transferRecipientArg()
    private val amount = checkNotNull(savedStateHandle.get<Long>(ARG_AMOUNT)) { "amount 인자 누락" }

    /**
     * 멱등성 키는 "이 송금 한 건"에 묶여 재시도 내내 동일해야 한다. 재시도마다 새로 만들면
     * 타임아웃 뒤 재시도가 서버엔 새 거래(=새 bank_tran_id)로 보여 이중출금을 못 막는다.
     */
    private val idempotencyKey: String =
        savedStateHandle.get<String>(IDEMPOTENCY_KEY) ?: UUID.randomUUID().toString().also {
            savedStateHandle[IDEMPOTENCY_KEY] = it
        }

    private val store = MviStore<ResultState, ResultAction, ResultEffect>(
        initialState = ResultState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            ResultIntent.BackClicked, ResultIntent.ConfirmClicked ->
                sendEffect(ResultEffect.Finish(sourceAccountId.value))

            ResultIntent.ShareClicked -> sendEffect(ResultEffect.Share)

            ResultIntent.LeaveMemoClicked -> sendEffect(ResultEffect.LeaveMemo)

            ResultIntent.RetryClicked -> {
                // 단발 가드: 이미 재실행 중이면 무시한다. 로딩 중 버튼을 숨기는 UI 가드는 재합성
                // 타이밍에 의존해 빠른 연타를 못 막으므로, 상태로 직접 재진입을 차단한다.
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

    /** 두 계좌를 조회해 송금 요청을 만들고 실행한 뒤, 결과를 phase로 반영한다. */
    private fun execute() {
        viewModelScope.launch {
            val source = accountRepository.findAccount(sourceAccountId)
            val toBankCode = BankCode.byCode(recipient.bankCode)
            if (source == null || toBankCode == null) {
                Log.e(TAG, "출금계좌/수취 은행 조회 실패 (source=$source, bank=${recipient.bankCode})")
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
        const val IDEMPOTENCY_KEY = "transfer_idempotency_key"
    }
}
