package com.study.bank.feature.transfer.accountinput.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.transfer.accountinput.contract.AccountInputEffect
import com.study.bank.feature.transfer.navigation.TransferAmountRoute
import com.study.bank.feature.transfer.navigation.amountRoute

@Composable
fun AccountInputRoute(
    onBack: () -> Unit,
    onResolved: (TransferAmountRoute) -> Unit,
    viewModel: AccountInputViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                AccountInputEffect.NavigateBack -> onBack()
                is AccountInputEffect.NavigateToAmount ->
                    onResolved(amountRoute(effect.sourceAccountId, effect.recipient))
            }
        }
    }

    AccountInputScreen(state = state, onIntent = viewModel::onIntent)
}
