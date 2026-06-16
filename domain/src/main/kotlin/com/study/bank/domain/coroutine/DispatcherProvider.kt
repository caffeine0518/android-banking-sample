package com.study.bank.domain.coroutine

import kotlinx.coroutines.CoroutineDispatcher

/**
 * 코루틴 디스패처 묶음을 추상화한 포트. 구현은 인프라(data-di)가 제공한다.
 *
 * 한정자(@Qualifier) 대신 **타입 주입**이라, 단위 테스트에서는 세 디스패처를 모두 하나의
 * TestDispatcher로 돌려주는 구현으로 한 번에 교체할 수 있다.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
