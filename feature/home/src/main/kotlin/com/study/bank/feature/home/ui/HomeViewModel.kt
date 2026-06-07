package com.study.bank.feature.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.feature.home.contract.HomeEffect
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val store = MviStore<HomeState, HomeIntent, HomeEffect>(
        initialState = HomeState(isLoading = true),
        scope = viewModelScope,
    ) { intent ->
        when (intent) {
            HomeIntent.Load -> loadAccounts()
            HomeIntent.Refresh -> loadAccounts()
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

    private suspend fun MviStore<HomeState, HomeIntent, HomeEffect>.loadAccounts() {
        setState { copy(isLoading = true) }
        accountRepository.observeAccounts()
            .catch { error ->
                Log.e(TAG, "Failed to load accounts", error)
                setState { copy(isLoading = false) }
            }
            .collect { accounts ->
                setState {
                    copy(
                        accounts = accounts.map { it.toUi() },
                        isLoading = false,
                    )
                }
            }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
