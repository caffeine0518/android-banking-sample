package com.study.bank.domain.coroutine

import kotlin.coroutines.cancellation.CancellationException

/**
 * 코루틴 협력 취소를 삼키지 않는 [runCatching] 대체본.
 *
 * 표준 [runCatching]은 [Throwable]을 전부 잡아 [CancellationException]까지 [Result.failure]로
 * 바꾼다. 그래서 스코프가 취소된 시점(예: ViewModel clear, 화면 이탈)에도 블록 이후 로직이
 * "실패"로 계속 흘러 `sendIntent`·재시도 등 죽은 스코프에서의 후속 작업이 일어나 구조적
 * 동시성이 깨진다. 이 함수는 [CancellationException]만 되던져 취소를 정상 전파하고, 나머지
 * 예외만 [Result]로 감싼다.
 *
 * [runCatching]처럼 `inline`이므로 블록 안에서 suspend 함수를 그대로 호출할 수 있다.
 */
inline fun <T> cancellableCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
