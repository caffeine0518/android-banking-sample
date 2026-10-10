package com.study.bank.core.ui.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviStoreTest {

    @Test
    fun `초기 state가 state Flow에 그대로 노출된다`() = runTest {
        val store = counterStore(initial = 7)

        assertEquals(7, store.state.first().count)
    }

    @Test
    fun `sendIntent가 reducer를 호출해 setState 결과가 state에 반영된다`() = runTest {
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.Increment -> setState { copy(count = count + 1) }
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.Increment)
        runCurrent()

        assertEquals(1, store.state.value.count)
    }

    @Test
    fun `reducer 안에서 sendEffect를 호출하면 effect Flow로 emit된다`() = runTest {
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.Beep -> sendEffect(CounterEffect.Beeped)
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.Beep)
        runCurrent()

        assertEquals(CounterEffect.Beeped, store.effect.first())
    }

    @Test
    fun `effect는 한 번 소비되면 재방출되지 않는다`() = runTest {
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.Beep -> sendEffect(CounterEffect.Beeped)
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.Beep)
        runCurrent()

        assertEquals(CounterEffect.Beeped, store.effect.first())

        assertNull(
            "소비된 effect가 재방출되면 one-shot 불변식이 깨진 것",
            withTimeoutOrNull(1_000) { store.effect.first() },
        )
    }

    @Test
    fun `reducer 안에서 sendIntent로 chained intent를 발행할 수 있다`() = runTest {
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.DoubleStep -> {
                    setState { copy(count = count + 1) }
                    sendIntent(CounterIntent.Increment)
                }
                CounterIntent.Increment -> setState { copy(count = count + 1) }
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.DoubleStep)
        runCurrent()

        assertEquals(2, store.state.value.count)
    }

    // reducer가 non-suspend이고 consumer가 하나라 한 reducer는 끼어듦 없이 끝까지 실행된다.

    @Test
    fun `여러 intent가 큐에 쌓이면 FIFO로 한 reducer가 끝난 뒤 다음 reducer가 진입한다`() = runTest {
        val log = mutableListOf<String>()
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.DoubleStep -> {
                    log += "double:start(count=${state.count})"
                    setState { copy(count = 10) }
                    log += "double:mid(count=${state.count})"
                    setState { copy(count = 20) }
                    log += "double:end(count=${state.count})"
                }
                CounterIntent.Increment -> {
                    log += "inc(count=${state.count})"
                    setState { copy(count = state.count + 1) }
                }
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.DoubleStep)
        store.sendIntent(CounterIntent.Increment)
        runCurrent()

        assertEquals(
            "DoubleStep 본문이 끝까지 실행된 뒤에야 Increment가 reducer에 진입해야 함",
            listOf(
                "double:start(count=0)",
                "double:mid(count=10)",
                "double:end(count=20)",
                "inc(count=20)",
            ),
            log,
        )
        assertEquals("DoubleStep 결과 20 + Increment의 +1", 21, store.state.value.count)
    }

    @Test
    fun `reducer 안 sendIntent는 enqueue만 할 뿐 본문을 즉시 끊지 않는다`() = runTest {
        val store = counterStore { intent ->
            when (intent) {
                CounterIntent.DoubleStep -> {
                    sendIntent(CounterIntent.Increment)   // 본문 중간에서 self-enqueue
                    setState { copy(count = 5) }          // 동기 호출이었다면 위 +1(→1)이 이 5에 덮어써짐
                }
                CounterIntent.Increment -> setState { copy(count = count + 1) }
                else -> Unit
            }
        }

        store.sendIntent(CounterIntent.DoubleStep)
        runCurrent()

        assertEquals(6, store.state.value.count)
    }

    @Test
    fun `동시 enqueue에도 가드는 첫 intent만 통과시킨다`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        try {
            val store = counterStore(scope, Dispatchers.Default) { intent ->
                when (intent) {
                    CounterIntent.Increment -> {
                        if (state.count > 0) return@counterStore
                        setState { copy(count = count + 1) }
                    }
                    CounterIntent.Beep -> sendEffect(CounterEffect.Beeped)   // drain 완료 신호
                    else -> Unit
                }
            }

            (1..10_000).map {
                async(Dispatchers.Default) { store.sendIntent(CounterIntent.Increment) }
            }.awaitAll()
            // FIFO라 Beep 수신은 앞선 1만 건의 처리 완료를 뜻한다.
            store.sendIntent(CounterIntent.Beep)
            withTimeout(5_000) { store.effect.first() }

            assertEquals("동시 enqueue에도 가드는 정확히 한 번만 통과해야 한다", 1, store.state.value.count)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `1만개 동시 Increment에도 lost update 없이 카운트가 일치한다`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        try {
            val store = counterStore(scope, Dispatchers.Default) { intent ->
                when (intent) {
                    CounterIntent.Increment -> setState { copy(count = count + 1) }
                    else -> Unit
                }
            }

            val total = 10_000
            (1..total).map {
                async(Dispatchers.Default) {
                    store.sendIntent(CounterIntent.Increment)
                }
            }.awaitAll()

            val settled = withTimeout(5_000) {
                store.state.first { it.count == total }
            }

            assertEquals(total, settled.count)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `MviStore는 setState와 sendEffect를 public API로 노출하지 않는다`() {
        val publicMethods = MviStore::class.java.methods.map { it.name }.toSet()

        assertFalse(
            "setState가 MviStore의 public API로 노출되면 ReducerScope 우회가 가능해진다",
            "setState" in publicMethods,
        )
        assertFalse(
            "sendEffect가 MviStore의 public API로 노출되면 ReducerScope 우회가 가능해진다",
            "sendEffect" in publicMethods,
        )
        assertTrue(
            "sendIntent는 외부 호출 entry point로 노출돼야 한다",
            "sendIntent" in publicMethods,
        )
    }

    private data class Counter(val count: Int)

    private sealed interface CounterIntent {
        data object Increment : CounterIntent
        data object DoubleStep : CounterIntent
        data object Beep : CounterIntent
    }

    private sealed interface CounterEffect {
        data object Beeped : CounterEffect
    }

    private fun TestScope.counterStore(
        initial: Int = 0,
        reducer: ReducerScope<Counter, CounterIntent, CounterEffect>.(CounterIntent) -> Unit = { },
    ): MviStore<Counter, CounterIntent, CounterEffect> =
        counterStore(
            scope = backgroundScope,
            dispatcher = StandardTestDispatcher(testScheduler),
            initial = initial,
            reducer = reducer
        )

    private fun counterStore(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        initial: Int = 0,
        reducer: ReducerScope<Counter, CounterIntent, CounterEffect>.(CounterIntent) -> Unit = { },
    ): MviStore<Counter, CounterIntent, CounterEffect> = MviStore(
        initialState = Counter(initial),
        scope = scope,
        dispatcher = dispatcher,
        reducer = reducer,
    )
}
