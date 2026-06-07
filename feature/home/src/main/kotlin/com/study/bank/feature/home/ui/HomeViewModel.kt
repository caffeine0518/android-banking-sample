package com.study.bank.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.study.bank.core.ui.mvi.MviStore
import com.study.bank.feature.home.contract.HomeEffect
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val store = MviStore<HomeState, HomeIntent, HomeEffect>(
        initialState = HomeState(),
        scope = viewModelScope,
    ) { intent ->
        when (intent) {
            HomeIntent.Load -> {
                // TODO: AccountRepository 연결 후 실제 계좌 목록 로드
            }
            HomeIntent.Refresh -> {
                // TODO: AccountRepository 연결 후 새로고침 구현
            }

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
}
