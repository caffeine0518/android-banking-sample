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
import kotlinx.coroutines.coroutineScope
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
    private val localeTargetCurrency: LocaleTargetCurrency,
) : ViewModel() {

    private val store = MviStore<HomeState, HomeIntent, HomeEffect>(
        initialState = HomeState(isLoading = true),
        scope = viewModelScope,
    ) { intent ->
        when (intent) {
            HomeIntent.Load -> loadHome()
            HomeIntent.Refresh -> loadHome()
            is HomeIntent.AccountClicked ->
                sendEffect(HomeEffect.NavigateToAccountDetail(intent.accountId))
        }
    }

    val state: StateFlow<HomeState> = store.state
    val effect: Flow<HomeEffect> = store.effect

    init {
        store.sendIntent(HomeIntent.Load)
    }

    fun onIntent(intent: HomeIntent) {
        store.sendIntent(intent)
    }

    private suspend fun MviStore<HomeState, HomeIntent, HomeEffect>.loadHome() {
        setState { copy(isLoading = true) }
        val target = localeTargetCurrency.resolve()
        coroutineScope {
            launch { collectAccounts() }
            launch { collectTotalAssets(target) }
        }
        setState { copy(isLoading = false) }
    }

    private suspend fun MviStore<HomeState, HomeIntent, HomeEffect>.collectAccounts() {
        accountRepository.observeAccounts()
            .catch { error -> Log.e(TAG, "Failed to load accounts", error) }
            .collect { accounts ->
                setState { copy(accounts = accounts.map(accountUiMapper::map)) }
            }
    }

    private suspend fun MviStore<HomeState, HomeIntent, HomeEffect>.collectTotalAssets(
        target: Currency,
    ) {
        totalAssetsUseCase(target)
            .catch { error -> Log.e(TAG, "Failed to load total assets", error) }
            .collect { totals ->
                setState {
                    copy(
                        totalAssets = moneyUiMapper.map(totals.converted),
                        unconvertedAssets = totals.unconverted.map(moneyUiMapper::map),
                    )
                }
            }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
