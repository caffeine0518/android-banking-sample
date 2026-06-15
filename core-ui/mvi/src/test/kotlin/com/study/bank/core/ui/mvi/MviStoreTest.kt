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

/**
 * 주입한 [StandardTestDispatcher] 위에서 sendIntent→[runCurrent]로 reducer 결과를 결정적으로 검증한다.
 * 가상 시간으론 못 잡는 멀티스레드 동작은 맨 아래 real-thread 테스트들에서 실제 Dispatchers.Default로 검증.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MviStoreTest {

    // ----- 초기 상태 & 기본 흐름 -----

    @Test
    fun `초기 state가 state Flow에 그대로 노출된다`() = runTest {
        val store = counterStore(initial = 7)

        // StateFlow는 collect 즉시 current value를 emit → first()로 확인
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

    // ----- sendEffect: one-shot channel 흐름 -----

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

        // 버퍼에 적재된 첫 element 소비
        assertEquals(CounterEffect.Beeped, store.effect.first())

        // effect는 one-shot(StateFlow와 달리 재방출 없음) → 두 번째 수신은 빈 채널에서 대기 → null이 정상.
        // (이 one-shot 보장이 effect를 Channel로 둔 이유)
        assertNull(
            "소비된 effect가 재방출되면 one-shot 불변식이 깨진 것",
            withTimeoutOrNull(1_000) { store.effect.first() },
        )
    }

    // ----- ReducerScope의 sendIntent: chained intent -----

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

        // chained intent까지 처리되어 +2
        assertEquals(2, store.state.value.count)
    }

    // ----- Multi-step transition 무결성 -----
    //
    // 한 reducer가 시작하면 끝까지 atomic하게 완결됨을 로그 시퀀스로 고정.
    // 근거: (a) reducer가 non-suspend라 끼어듦 불가 (b) consumer가 단일 launch라 동시 진행 불가.

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

        // 둘 다 enqueue한 뒤 runCurrent → consumer가 둘을 FIFO로 소진(StandardTestDispatcher는 eager 아님)
        store.sendIntent(CounterIntent.DoubleStep)
        store.sendIntent(CounterIntent.Increment)
        runCurrent()

        // 끼어듦이 있었다면 mid/end 사이에 inc 로그가 박혔을 것
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

        // enqueue라서 Increment는 DoubleStep 본문이 끝난 뒤 실행 → 5 다음 +1 = 6.
        // (동기 호출이었다면 +1이 먼저 적용됐다가 setState(5)에 덮여 5가 됐을 것)
        assertEquals(6, store.state.value.count)
    }

    // ----- 진짜 멀티스레드: 단일 consumer가 reducer를 직렬화 → in-reducer 가드는 TOCTOU-safe -----
    //
    // 가드(read state → check → write)는 본래 동시 환경에서 TOCTOU race가 나는 패턴이다.
    // 하지만 MviStore는 모든 reducer를 단일 consumer에서 순차 실행하므로, 여러 스레드가 동시에
    // enqueue해도 read→write가 끼어듦 없이 직렬화된다 → 가드는 정확히 한 번만 통과.
    // 설계가 깨져 reducer가 동시 실행되면 여러 건이 count==0을 보고 통과 → 최종 count가 1을 넘어 fail.

    @Test
    fun `동시 enqueue에도 가드는 첫 intent만 통과시킨다`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        try {
            val store = counterStore(scope, Dispatchers.Default) { intent ->
                when (intent) {
                    CounterIntent.Increment -> {
                        if (state.count > 0) return@counterStore   // read + 가드
                        setState { copy(count = count + 1) }        // write — 통과 횟수가 count에 누적
                    }
                    CounterIntent.Beep -> sendEffect(CounterEffect.Beeped)   // drain 완료 신호
                    else -> Unit
                }
            }

            // 1만개를 여러 스레드에서 동시 enqueue. awaitAll 시점에 전부 채널에 적재됨.
            (1..10_000).map {
                async(Dispatchers.Default) { store.sendIntent(CounterIntent.Increment) }
            }.awaitAll()
            // FIFO라 이 Beep은 1만개가 전부 처리된 뒤 처리됨 → effect 수신으로 drain 완료를 관측.
            store.sendIntent(CounterIntent.Beep)
            withTimeout(5_000) { store.effect.first() }

            // 직렬화 덕에 동시 enqueue여도 가드 통과는 1회뿐 → count는 정확히 1.
            assertEquals("동시 enqueue에도 가드는 정확히 한 번만 통과해야 한다", 1, store.state.value.count)
        } finally {
            scope.cancel()
        }
    }

    // ----- 진짜 멀티스레드 stress -----
    //
    // 가상 시간으론 못 잡는 race를 Dispatchers.Default 풀에서 1만개 동시 sendIntent로 검증.
    // 무손실 근거: (a) Channel.UNLIMITED라 동시 trySend가 thread-safe (b) 단일 consumer가 FIFO로 전부 소진.
    // (setState CAS는 단일 consumer라 경합 자체가 없음). 유실되면 count가 total에 못 도달 → withTimeout fail.

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
            // 1만개 코루틴이 동시 sendIntent (trySend는 thread-safe)
            (1..total).map {
                async(Dispatchers.Default) {
                    store.sendIntent(CounterIntent.Increment)
                }
            }.awaitAll()

            // 모두 처리될 때까지 wait. 유실 시 total에 못 도달 → withTimeout이 fail시킴
            val settled = withTimeout(5_000) {
                store.state.first { it.count == total }
            }

            assertEquals(total, settled.count)
        } finally {
            scope.cancel()
        }
    }

    // ----- public API 표면 회귀 방지 -----
    //
    // setState/sendEffect는 ReducerScope 멤버라 MviStore surface엔 없다(외부 호출 시 컴파일 에러).
    // 누군가 MviStore에 public fun setState를 추가하는 회귀를 reflection으로 잡는 안전망.
    // 한계: 이름 기반이라 internal fun도 바이트코드상 public으로 잡힐 수 있음(현재는 무해).

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
        // sendIntent는 외부에서 reducer 입력을 enqueue하는 정식 entry point이므로 public이어야 한다
        assertTrue(
            "sendIntent는 외부 호출 entry point로 노출돼야 한다",
            "sendIntent" in publicMethods,
        )
    }

    // ----- 테스트 픽스처 -----

    private data class Counter(val count: Int)

    private sealed interface CounterIntent {
        data object Increment : CounterIntent
        data object DoubleStep : CounterIntent
        data object Beep : CounterIntent
    }

    private sealed interface CounterEffect {
        data object Beeped : CounterEffect
    }

    // store 생성 보일러플레이트 헬퍼. StandardTestDispatcher(testScheduler)를 주입해 runCurrent()가
    // consumer를 결정적으로 구동하게 하고, backgroundScope로 끝나지 않는 consumer 루프의 leak을 막는다.
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

    // scope·dispatcher를 주입하는 오버로드. stress test가 실제 Dispatchers.Default를 넣을 때 쓴다.
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
