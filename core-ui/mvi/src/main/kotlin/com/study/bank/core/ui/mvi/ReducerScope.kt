package com.study.bank.core.ui.mvi

/** state 변경·effect 발행은 reducer 안에서만 가능하다. 외부 스트림은 [sendIntent]로 reducer를 거친다. */
interface ReducerScope<S, I, E> {

    val state: S

    fun setState(block: S.() -> S)

    fun sendEffect(effect: E)

    fun sendIntent(intent: I)
}
