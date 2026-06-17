package com.study.bank.data.di.coroutine

import com.study.bank.domain.coroutine.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 앱 전역 [DispatcherProvider] 바인딩. 테스트에서는 ViewModel 생성자로 테스트 구현을 직접 주입한다. */
@Module
@InstallIn(SingletonComponent::class)
internal object DispatchersModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
