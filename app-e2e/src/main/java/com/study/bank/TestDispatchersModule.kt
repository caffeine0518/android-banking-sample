package com.study.bank

import com.study.bank.data.di.coroutine.DispatchersModule
import com.study.bank.domain.coroutine.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * E2E 한정 [DispatcherProvider] — 프로덕션 [DispatchersModule]을 교체한다.
 *
 * 왜 필요한가: 프로덕션은 `default`가 [Dispatchers.Default]라, [com.study.bank.core.ui.mvi.MviStore]의
 * reducer 코루틴(`for (intent in intents)` — 절대 끝나지 않음)이 **백그라운드 스레드**에서 돈다.
 * 계측 테스트에서는 이 백그라운드 코루틴이 Compose 테스트 클럭/idle 동기화 밖에 있어, 화면을 떠나는
 * 동안 navigation-compose의 진입 전환(SeekableTransitionState.animateTo)이 settle되지 못한다 →
 * 진입 NavBackStackEntry가 maxLifecycle=INITIALIZED에 갇히고, Activity 파괴 시 INITIALIZED→DESTROYED
 * 전이 위반으로 크래시한다("State must be at least 'CREATED'…").
 *
 * `default`를 메인 디스패처에 묶으면 reducer가 테스트가 제어하는 메인에서 돌아 전환이 정상 settle된다.
 * `io`는 실제 네트워크(MockWebServer)용이라 메인에 묶지 않는다(NetworkOnMainThread 회피).
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DispatchersModule::class])
object TestDispatchersModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.IO
        override val default: CoroutineDispatcher = Dispatchers.Main
    }
}
