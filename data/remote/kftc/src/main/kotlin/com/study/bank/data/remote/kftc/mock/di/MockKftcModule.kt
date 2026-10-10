package com.study.bank.data.remote.kftc.mock.di

import android.content.Context
import com.study.bank.data.remote.kftc.mock.seed.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.storage.MockKftcDatabase
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.seed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object MockKftcModule {

    /** DAO가 모두 같은 DB를 쓰도록 [Singleton]이어야 한다. */
    @Provides
    @Singleton
    fun provideMockKftcDatabase(
        @ApplicationContext context: Context,
        accountSeed: List<SeedAccount>,
    ): MockKftcDatabase = MockKftcDatabase.inMemory(context).also { it.seed(accountSeed) }

    @Provides
    fun provideMockTransactionScopeDao(database: MockKftcDatabase): MockTransactionScopeDao =
        database.transactionScopeDao()

    @Provides
    fun provideMockAccountDao(database: MockKftcDatabase): MockAccountDao = database.accountDao()

    @Provides
    fun provideMockTransactionDao(database: MockKftcDatabase): MockTransactionDao =
        database.transactionDao()

    @Provides
    fun provideMockWithdrawalDao(database: MockKftcDatabase): MockWithdrawalDao =
        database.withdrawalDao()

    @Provides
    fun provideAccountSeed(): List<SeedAccount> = KftcAccountSeed.accounts
}
