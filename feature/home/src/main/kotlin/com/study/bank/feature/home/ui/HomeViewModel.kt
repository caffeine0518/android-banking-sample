package com.study.bank.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.model.Currency
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.usecase.account.TotalAssetsUseCase
import com.study.bank.feature.home.contract.HomeAction
import com.study.bank.feature.home.contract.HomeEffect
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeInternalAction
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.model.AccountUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val totalAssetsUseCase: TotalAssetsUseCase,
    private val accountUiMapper: AccountUiMapper,
    private val moneyUiMapper: MoneyUiMapper,
    localeTargetCurrency: LocaleTargetCurrency,
) : ViewModel() {

    private val store = MviStore<HomeState, HomeAction, HomeEffect>(
        initialState = HomeState(),
        scope = viewModelScope,
    ) { action ->
        when (action) {
            HomeIntent.Refresh -> {
                if (state.isLoading) return@MviStore
                setState { copy(isLoading = true) }
                startRefresh()
            }

            is HomeIntent.AccountClicked -> {
                sendEffect(HomeEffect.NavigateToAccountDetail(action.accountId))
            }

            is HomeInternalAction.AccountsUpdated -> {
                setState {
                    copy(accounts = action.accounts.map(accountUiMapper::map))
                }
            }

            is HomeInternalAction.TotalAssetsUpdated -> {
                setState {
                    copy(
                        totalAssets = moneyUiMapper.map(action.totals.converted),
                        unconvertedAssets = action.totals.unconverted.map(moneyUiMapper::map),
                    )
                }
            }

            is HomeInternalAction.RefreshFinished -> {
                if (action.error != null) sendEffect(HomeEffect.ShowRefreshError)
                setState { copy(isLoading = false) }
            }
        }
    }

    val state: StateFlow<HomeState> = store.state
    val effect: Flow<HomeEffect> = store.effect

    init {
        collectAccounts()
        collectTotalAssets(localeTargetCurrency.resolve())
        store.sendIntent(HomeIntent.Refresh)
    }

    fun onIntent(intent: HomeIntent) {
        store.sendIntent(intent)
    }

    private fun startRefresh() {
        viewModelScope.launch {
            val error = runCatching { accountRepository.refresh() }
                .exceptionOrNull()
                ?.also { Log.e(TAG, "refresh failed", it) }
            store.sendIntent(HomeInternalAction.RefreshFinished(error))
        }
    }

    private fun collectAccounts() {
        viewModelScope.launch {
            accountRepository.observeAccounts()
                .catch { error -> Log.e(TAG, "Failed to observe accounts", error) }
                .collect { accounts ->
                    store.sendIntent(HomeInternalAction.AccountsUpdated(accounts))
                }
        }
    }

    private fun collectTotalAssets(target: Currency) {
        viewModelScope.launch {
            totalAssetsUseCase(target)
                .catch { error -> Log.e(TAG, "Failed to observe total assets", error) }
                .collect { totals ->
                    store.sendIntent(HomeInternalAction.TotalAssetsUpdated(totals))
                }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
