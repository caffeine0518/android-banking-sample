package com.study.bank.feature.transfer.recipient.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.transfer.navigation.TransferAmountRoute
import com.study.bank.feature.transfer.navigation.amountRoute
import com.study.bank.feature.transfer.recipient.contract.RecipientEffect

@Composable
fun RecipientRoute(
    onBack: () -> Unit,
    onAccountNumberInput: (sourceAccountId: String) -> Unit,
    onContinue: (TransferAmountRoute) -> Unit,
    viewModel: RecipientViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                RecipientEffect.NavigateBack -> onBack()
                is RecipientEffect.NavigateToAccountNumberInput ->
                    onAccountNumberInput(effect.sourceAccountId)
                is RecipientEffect.NavigateToAmount ->
                    onContinue(amountRoute(effect.sourceAccountId, effect.recipient))
            }
        }
    }

    RecipientScreen(state = state, onIntent = viewModel::onIntent)
}
