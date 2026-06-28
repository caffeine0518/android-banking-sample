package com.study.bank.feature.account.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.account.R
import com.study.bank.feature.account.contract.AccountDetailEffect

@Composable
fun AccountDetailRoute(
    onSendClick: (AccountId) -> Unit,
    onBack: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshErrorMessage = stringResource(R.string.account_refresh_error)
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AccountDetailEffect.NavigateToTransfer -> onSendClick(AccountId(effect.accountId))
                AccountDetailEffect.NavigateBack -> onBack()
                AccountDetailEffect.ShowRefreshError -> snackbarHostState.showSnackbar(refreshErrorMessage)
            }
        }
    }

    AccountDetailScreen(
        state = state,
        transactions = viewModel.transactions,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}
