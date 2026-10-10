package com.study.bank.domain.coroutine

import kotlin.coroutines.cancellation.CancellationException

/**
 * [CancellationException]은 다시 전파하는 [runCatching].
 *
 * 표준 [runCatching]은 취소까지 실패로 바꿔, 취소된 스코프에서도 이후 로직(재시도 등)이 실행된다.
 */
inline fun <T> cancellableCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
