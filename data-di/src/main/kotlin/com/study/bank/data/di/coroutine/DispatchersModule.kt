package com.study.bank.data.di.coroutine

import com.study.bank.domain.coroutine.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 전역 [DispatcherProvider] 바인딩. 단위 테스트는 ViewModel 생성자로 테스트 구현을 직접 주입하고,
 * 계측(E2E) 테스트는 이 모듈을 @TestInstallIn으로 교체해 디스패처를 메인에 묶는다(전환 동기화).
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
