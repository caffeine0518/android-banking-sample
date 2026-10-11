package com.study.bank.data.di.kftc

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.api.KftcApiServiceImpl
import com.study.bank.data.remote.kftc.corebanking.api.CoreBankingApiService
import com.study.bank.data.remote.kftc.corebanking.api.CoreBankingApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {

    @Binds
    @Singleton
    internal abstract fun bindKftcApiService(impl: KftcApiServiceImpl): KftcApiService

    @Binds
    @Singleton
    internal abstract fun bindCoreBankingApiService(impl: CoreBankingApiServiceImpl): CoreBankingApiService
}
