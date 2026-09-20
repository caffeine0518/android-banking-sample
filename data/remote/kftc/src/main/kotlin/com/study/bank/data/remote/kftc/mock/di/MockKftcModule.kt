package com.study.bank.data.remote.kftc.mock.di

import android.content.Context
import com.study.bank.data.remote.kftc.mock.seed.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.storage.MockKftcDatabase
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import com.study.bank.data.remote.kftc.mock.storage.seed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Mock 은행 DB와 그 DAO들을 그래프에 노출한다 — `:data-di`의 LocalModule이 앱 캐시에 하는 것과 같은 모양.
 *
 * mock 내부 타입(internal)을 바인딩하므로 `:data-di`가 아니라 이 모듈 안에 둔다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object MockKftcModule {

    /**
     * DAO 넷이 같은 인스턴스를 보게 [Singleton]이어야 한다 — 아니면 계좌와 원장이 서로 다른 DB에 쌓인다.
     * 첫 요청 전에 시드가 들어가도록 생성 직후 적재한다.
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

    /** 부팅 시 테이블을 채우는 계좌 시드. 테스트는 DB를 직접 만들어 다른 시드를 적재한다. */
    @Provides
    fun provideAccountSeed(): List<SeedAccount> = KftcAccountSeed.accounts
}
