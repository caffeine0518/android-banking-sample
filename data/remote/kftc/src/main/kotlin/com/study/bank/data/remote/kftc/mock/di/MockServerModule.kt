package com.study.bank.data.remote.kftc.mock.di

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.mock.KftcMockServerImpl
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** mock 내부 구현체 바인딩. 구현체가 internal이라 `:data-di`가 아니라 이 모듈 안에 둔다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockServerModule {

    @Binds
    @Singleton
    internal abstract fun bindKftcMockServer(impl: KftcMockServerImpl): KftcMockServer

    /** 상태를 원장(Room)에만 두므로 스코프 없이 바인딩한다. */
    @Binds
    internal abstract fun bindKftcWithdrawalService(
        impl: KftcWithdrawalServiceImpl,
    ): KftcWithdrawalService
}
