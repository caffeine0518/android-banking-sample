package com.study.bank.data.remote.kftc.mock.di

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.mock.KftcMockServerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 구현체가 internal이라 `:data-di`가 아니라 이 모듈 안에서 바인딩한다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockServerModule {

    @Binds
    @Singleton
    internal abstract fun bindKftcMockServer(impl: KftcMockServerImpl): KftcMockServer
}
