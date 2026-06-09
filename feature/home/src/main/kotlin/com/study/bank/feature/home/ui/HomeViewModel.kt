package com.study.bank.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.model.Currency
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.usecase.account.TotalAssetsUseCase
import com.study.bank.feature.home.contract.HomeEffect
import com.study.bank.feature.home.contract.HomeIntent
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

    private val store = MviStore<HomeState, HomeIntent, HomeEffect>(
        initialState = HomeState(isLoading = true),
        scope = viewModelScope,
    ) { intent ->
        when (intent) {
            HomeIntent.Refresh -> refresh()
            is HomeIntent.AccountClicked ->
                sendEffect(HomeEffect.NavigateToAccountDetail(intent.accountId))
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

    private suspend fun MviStore<HomeState, HomeIntent, HomeEffect>.refresh() {
        setState { copy(isLoading = true) }
        runCatching {
            accountRepository.refresh()
        }.onFailure { error ->
            Log.e(TAG, "refresh failed", error)
        }
        setState { copy(isLoading = false) }
    }

    private fun collectAccounts() {
        viewModelScope.launch {
            accountRepository.observeAccounts()
                .catch { error -> Log.e(TAG, "Failed to observe accounts", error) }
                .collect { accounts ->
                    store.setState { copy(accounts = accounts.map(accountUiMapper::map)) }
                }
        }
    }

    private fun collectTotalAssets(target: Currency) {
        viewModelScope.launch {
            totalAssetsUseCase(target)
                .catch { error -> Log.e(TAG, "Failed to observe total assets", error) }
                .collect { totals ->
                    store.setState {
                        copy(
                            totalAssets = moneyUiMapper.map(totals.converted),
                            unconvertedAssets = totals.unconverted.map(moneyUiMapper::map),
                        )
                    }
                }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
