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

/**
 * Mock 은행 DB와 DAO를 Hilt 그래프에 제공한다. `:data-di`의 LocalModule이 앱 캐시 DB를 제공하는 방식과 같다.
 *
 * mock 내부(internal) 타입을 바인딩하므로 `:data-di`가 아니라 이 모듈에 둔다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object MockKftcModule {

    /**
     * DAO 네 개가 같은 DB를 쓰도록 [Singleton]으로 제공한다. 그렇지 않으면 계좌와 원장이 서로 다른 DB에 저장된다.
     * 첫 요청 전에 데이터가 있도록 생성 직후 시드를 적재한다.
     */
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

    /** 앱 시작 시 적재하는 계좌 시드. 테스트는 DB를 직접 생성해 다른 시드를 적재한다. */
    @Provides
    fun provideAccountSeed(): List<SeedAccount> = KftcAccountSeed.accounts
}
