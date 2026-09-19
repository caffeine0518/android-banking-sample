package com.study.bank.domain.coroutine

import kotlin.coroutines.cancellation.CancellationException

/**
 * 코루틴 협력 취소를 무시하지 않는 [runCatching] 대체본.
 *
 * 표준 [runCatching]은 [CancellationException]까지 [Result.failure]로 바꾼다. 그래서 스코프가 취소된
 * 시점(예: ViewModel clear, 화면 이탈)에도 블록 이후 로직이 "실패"로 이어져, 종료된 스코프에서
 * `sendIntent`·재시도가 발생하고 구조적 동시성이 무너진다.
 *
 * [runCatching]처럼 `inline`이라 블록 안에서 suspend 함수를 그대로 호출할 수 있다.
 */
inline fun <T> cancellableCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
