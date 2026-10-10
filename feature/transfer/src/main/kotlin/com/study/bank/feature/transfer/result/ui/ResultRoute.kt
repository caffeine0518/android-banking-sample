package com.study.bank.feature.transfer.result.ui

import androidx.activity.compose.BackHandler
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
import com.study.bank.feature.transfer.navigation.TransferResultRoute
import com.study.bank.feature.transfer.result.contract.ResultEffect
import com.study.bank.feature.transfer.result.contract.ResultPhase

@Composable
fun ResultRoute(
    route: TransferResultRoute,
    onFinish: (sourceAccountId: String) -> Unit,
    viewModel: ResultViewModel = hiltViewModel<ResultViewModel, ResultViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val effects = remember(viewModel.effect, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
    }

    ResultBackHandler(phase = state.phase)

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is ResultEffect.Finish -> currentOnFinish(effect.sourceAccountId)
            }
        }
    }

    ResultScreen(state = state, onIntent = viewModel::onIntent)
}

/**
 * 송금을 보낸 뒤(로딩/성공)에는 시스템 뒤로가기로 확인·금액 화면에 되돌아가 재송금하지 못하게 막는다.
 * 출금되지 않은 것이 확정된 실패일 때만 뒤로가기를 허용해 이전 화면에서 수정하게 둔다.
 * 반영 여부를 알 수 없는 실패에서 결과 화면을 벗어나면 멱등성 키가 제거되어 재송금이 새 거래로 체결된다.
 */
@Composable
internal fun ResultBackHandler(phase: ResultPhase) {
    BackHandler(enabled = !(phase is ResultPhase.Failure && phase.reason.isDefinite)) {
        // no-op: 로딩/성공/불확정 실패 중 뒤로가기 차단(완료는 "확인" 또는 상단 백으로만).
    }
}
