package com.study.bank.feature.account.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.feature.account.ui.model.TransactionUi
import com.study.bank.domain.coroutine.DispatcherProvider
import com.study.bank.domain.coroutine.cancellableCatching
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.TransactionRepository
import com.study.bank.feature.account.contract.AccountDetailAction
import com.study.bank.feature.account.contract.AccountDetailEffect
import com.study.bank.feature.account.contract.AccountDetailInternalAction
import com.study.bank.feature.account.contract.AccountDetailIntent
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.model.AccountUiMapper
import com.study.bank.feature.account.ui.model.TransactionUiMapper
import com.study.bank.feature.account.ui.navigation.ACCOUNT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val accountUiMapper: AccountUiMapper,
    private val transactionUiMapper: TransactionUiMapper,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    // 진입 시 네비게이션 인자로 받은 계좌 식별자(= fintech_use_num). 이 화면의 단일 대상.
    private val accountId = AccountId(
        checkNotNull(savedStateHandle.get<String>(ACCOUNT_ID_ARG)) { "accountId 인자 누락" },
    )

    private val store = MviStore<AccountDetailState, AccountDetailAction, AccountDetailEffect>(
        initialState = AccountDetailState(),
        scope = viewModelScope,
        dispatcher = dispatcherProvider.default,
    ) { action ->
        when (action) {
            AccountDetailIntent.Refresh -> {
                if (state.isLoading) return@MviStore
                setState { copy(isLoading = true) }
                startRefresh()
            }

            AccountDetailIntent.SendClicked -> {
                sendEffect(AccountDetailEffect.NavigateToTransfer(accountId.value))
            }

            AccountDetailIntent.BackClicked -> {
                sendEffect(AccountDetailEffect.NavigateBack)
            }

            is AccountDetailInternalAction.AccountUpdated -> {
                setState { copy(account = action.account?.let(accountUiMapper::map)) }
            }

            is AccountDetailInternalAction.RefreshFinished -> {
                if (action.error != null) sendEffect(AccountDetailEffect.ShowRefreshError)
                setState { copy(isLoading = false) }
            }
        }
    }

    val state: StateFlow<AccountDetailState> = store.state
    val effect: Flow<AccountDetailEffect> = store.effect

    val transactions: Flow<PagingData<TransactionUi>> =
        transactionRepository.transactionStream(accountId)
            .map { pagingData -> pagingData.map(transactionUiMapper::map) }
            .cachedIn(viewModelScope)

    init {
        collectAccount()
        store.sendIntent(AccountDetailIntent.Refresh)
    }

    fun onIntent(intent: AccountDetailIntent) {
        store.sendIntent(intent)
    }

    private fun startRefresh() {
        viewModelScope.launch {
            val error = cancellableCatching { accountRepository.refresh() }
                .exceptionOrNull()?.also { Log.e(TAG, "refresh failed", it) }
            store.sendIntent(AccountDetailInternalAction.RefreshFinished(error))
        }
    }

    private fun collectAccount() {
        viewModelScope.launch {
            accountRepository.observeAccount(accountId)
                .catch { error -> Log.e(TAG, "Failed to observe account", error) }
                .collect { account ->
                    store.sendIntent(AccountDetailInternalAction.AccountUpdated(account))
                }
        }
    }

    private companion object {
        const val TAG = "AccountDetailViewModel"
    }
}
