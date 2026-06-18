package com.study.bank.data.di.repository

import com.study.bank.data.repository.account.AccountRepositoryImpl
import com.study.bank.data.repository.fx.FxRateRepositoryImpl
import com.study.bank.data.repository.recipient.RecipientRepositoryImpl
import com.study.bank.data.repository.transaction.TransactionRepositoryImpl
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import com.study.bank.domain.repository.RecipientRepository
import com.study.bank.domain.repository.TransactionRepository
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

    @Binds
    internal abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    internal abstract fun bindRecipientRepository(impl: RecipientRepositoryImpl): RecipientRepository
}
