package com.study.bank.feature.transfer.confirm.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.transfer.confirm.contract.ConfirmEffect

@Composable
fun ConfirmRoute(
    onBack: () -> Unit,
    onSent: () -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                ConfirmEffect.NavigateBack -> onBack()
                ConfirmEffect.Submit -> onSent()
                // 편집/변경 화면 미구현 — 현재는 무시(placeholder).
                ConfirmEffect.EditDisplayName -> Unit
                ConfirmEffect.ChangeSource -> Unit
            }
        }
    }

    ConfirmScreen(state = state, onIntent = viewModel::onIntent)
}
