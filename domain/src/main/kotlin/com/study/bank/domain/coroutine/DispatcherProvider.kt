package com.study.bank.domain.coroutine

import kotlinx.coroutines.CoroutineDispatcher

/** 한정자 대신 타입으로 주입해, 테스트에서 세 디스패처를 TestDispatcher 하나로 교체할 수 있다. */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
