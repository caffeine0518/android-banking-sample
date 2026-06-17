package com.study.bank.data.di.repository

import com.study.bank.data.repository.account.AccountRepositoryImpl
import com.study.bank.data.repository.fx.FxRateRepositoryImpl
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    internal abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    internal abstract fun bindFxRateRepository(impl: FxRateRepositoryImpl): FxRateRepository
}
