package com.study.bank.feature.transfer.confirm.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.transfer.confirm.contract.ConfirmEffect
import com.study.bank.feature.transfer.navigation.TransferConfirmRoute
import com.study.bank.feature.transfer.navigation.TransferResultRoute

@Composable
fun ConfirmRoute(
    route: TransferConfirmRoute,
    onBack: () -> Unit,
    onSent: (TransferResultRoute) -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel<ConfirmViewModel, ConfirmViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnSent by rememberUpdatedState(onSent)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                ConfirmEffect.NavigateBack -> currentOnBack()
                is ConfirmEffect.Submit ->
                    currentOnSent(
                        TransferResultRoute(
                            sourceAccountId = effect.sourceAccountId,
                            recipient = effect.recipient,
                            amount = effect.amount,
                            idempotencyKey = effect.idempotencyKey,
                        ),
                    )
            }
        }
    }

    ConfirmScreen(state = state, onIntent = viewModel::onIntent)
}
