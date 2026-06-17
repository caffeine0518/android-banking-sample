package com.study.bank.data.di.fx

import com.study.bank.data.remote.fx.api.KeximApiService
import com.study.bank.data.remote.fx.api.KeximApiServiceImpl
import com.study.bank.data.remote.fx.api.KeximHttpApi
import com.study.bank.data.remote.fx.api.KeximHttpApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FxNetworkModule {

    @Binds
    @Singleton
    internal abstract fun bindKeximHttpApi(impl: KeximHttpApiImpl): KeximHttpApi

    @Binds
    @Singleton
    internal abstract fun bindKeximApiService(impl: KeximApiServiceImpl): KeximApiService
}
