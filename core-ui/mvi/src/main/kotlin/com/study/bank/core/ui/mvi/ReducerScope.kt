package com.study.bank.core.ui.mvi

/**
 * Reducer 안에서만 노출되는 state mutation 권한.
 *
 * 외부(viewModelScope.launch 블록, collect 등)에서는 [MviStore] 인스턴스로 setState/sendEffect를
 * 호출할 수 없어 "state mutation은 reducer 한 곳" 원칙을 컴파일 타임에 강제한다.
 * 외부 스트림 수신은 [sendIntent]로 reducer를 거치게 해야 한다.
 */
interface ReducerScope<S, I, E> {

    val state: S

    fun setState(block: S.() -> S)

    fun sendEffect(effect: E)

    fun sendIntent(intent: I)
}
