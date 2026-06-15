package com.study.bank.core.ui.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MviStore<S, I, E>(
    initialState: S,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val reducer: ReducerScope<S, I, E>.(I) -> Unit,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val intents = Channel<I>(Channel.UNLIMITED)

    private val _effect = Channel<E>(Channel.UNLIMITED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    private val reducerScope = object : ReducerScope<S, I, E> {
        override val state: S get() = _state.value
        override fun setState(block: S.() -> S) = _state.update(block)
        override fun sendEffect(effect: E) { _effect.trySend(effect) }
        override fun sendIntent(intent: I) = this@MviStore.sendIntent(intent)
    }

    init {
        scope.launch(dispatcher) {
            for (intent in intents) {
                reducer(reducerScope, intent)
            }
        }
    }

    fun sendIntent(intent: I) {
        intents.trySend(intent)
    }
}
