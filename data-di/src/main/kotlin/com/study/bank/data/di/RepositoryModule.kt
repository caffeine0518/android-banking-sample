package com.study.bank.data.di

import com.study.bank.data.repository.account.AccountRepositoryImpl
import com.study.bank.domain.repository.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}
