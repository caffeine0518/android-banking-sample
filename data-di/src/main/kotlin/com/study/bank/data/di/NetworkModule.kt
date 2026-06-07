package com.study.bank.data.di

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.api.KftcApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindKftcApiService(impl: KftcApiServiceImpl): KftcApiService
}
