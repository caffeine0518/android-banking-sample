package com.study.bank.feature.transfer.result.ui

import androidx.activity.compose.BackHandler
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
import com.study.bank.feature.transfer.result.contract.ResultPhase

@Composable
fun ResultRoute(
    onFinish: (sourceAccountId: String) -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    // 송금을 보낸 뒤(로딩/성공)에는 시스템 뒤로가기로 확인·금액 화면에 되돌아가 재송금하지 못하게 막는다.
    // 실패일 때만 뒤로가기를 허용해 이전 화면으로 돌아가게 둔다.
    BackHandler(enabled = state.phase !is ResultPhase.Failure) {
        // no-op: 로딩/성공 중 뒤로가기 차단(완료는 "확인" 또는 상단 백으로만).
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is ResultEffect.Finish -> onFinish(effect.sourceAccountId)
                // 공유/메모 화면 미구현 — 현재는 무시(placeholder).
                ResultEffect.Share -> Unit
                ResultEffect.LeaveMemo -> Unit
            }
        }
    }

    ResultScreen(state = state, onIntent = viewModel::onIntent)
}
