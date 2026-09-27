package com.study.bank.feature.account.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.account.R
import com.study.bank.feature.account.contract.AccountDetailEffect
import com.study.bank.feature.account.ui.navigation.AccountRoute

@Composable
fun AccountDetailRoute(
    route: AccountRoute,
    onSendClick: (accountId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel<AccountDetailViewModel, AccountDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSendClick by rememberUpdatedState(onSendClick)
    val currentOnBack by rememberUpdatedState(onBack)
    val refreshErrorMessage by rememberUpdatedState(stringResource(R.string.account_refresh_error))
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AccountDetailEffect.NavigateToTransfer -> currentOnSendClick(effect.accountId)
                AccountDetailEffect.NavigateBack -> currentOnBack()
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
