package com.study.bank.domain.coroutine

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class CancellableCatchingTest {

    @Test
    fun `returns success with the block result`() {
        val result = cancellableCatching { 42 }
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `wraps a regular throwable into failure`() {
        val boom = IllegalStateException("boom")
        val result = cancellableCatching { throw boom }
        assertTrue(result.isFailure)
        assertEquals(boom, result.exceptionOrNull())
    }

    @Test
    fun `rethrows CancellationException instead of swallowing it`() {
        assertFailsWith<CancellationException> {
            cancellableCatching { throw CancellationException("cancelled") }
        }
    }

    // 핵심 회귀 보호: 취소가 정상 전파되어 블록 이후 로직이 실행되지 않아야 한다.
    // 표준 runCatching이라면 취소를 삼켜 afterBlock이 true가 되며 테스트가 깨진다.
    @Test
    fun `cancellation propagates so post-block work does not run`() = runTest {
        val started = CompletableDeferred<Unit>()
        var afterBlock = false

        val job = launch {
            cancellableCatching {
                started.complete(Unit)
                awaitCancellation()
            }
            afterBlock = true
        }

        started.await()
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertFalse(afterBlock, "취소 후에는 cancellableCatching 이후 로직이 실행되면 안 된다")
    }
}
