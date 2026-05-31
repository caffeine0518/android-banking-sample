package com.study.bank.core.ui.mvi

import kotlinx.coroutines.CoroutineScope
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
    private val reducer: suspend MviStore<S, I, E>.(I) -> Unit,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    fun sendIntent(intent: I) {
        scope.launch { reducer(intent) }
    }

    fun setState(block: S.() -> S) = _state.update(block)
    suspend fun sendEffect(e: E) = _effect.send(e)
}
