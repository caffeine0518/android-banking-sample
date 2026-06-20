package com.study.bank.feature.transfer.result.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.study.bank.feature.transfer.result.contract.ResultEffect

@Composable
fun ResultRoute(
    onFinish: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                ResultEffect.Finish -> onFinish()
                // 공유/메모 화면 미구현 — 현재는 무시(placeholder).
                ResultEffect.Share -> Unit
                ResultEffect.LeaveMemo -> Unit
            }
        }
    }

    ResultScreen(state = state, onIntent = viewModel::onIntent)
}
