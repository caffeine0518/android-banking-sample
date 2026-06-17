package com.study.bank.data.di.local

import android.content.Context
import androidx.room.Room
import com.study.bank.data.local.BankDatabase
import com.study.bank.data.local.dao.AccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalModule {

    /**
     * KFTC가 mock이라 프로세스 재시작 시 시드가 초기화되므로 영속 캐시는 mock과 영구히 갈라짐.
     * 인메모리 DB로 lifecycle을 맞춰 불일치를 차단하면서 SSOT + DAO Flow 패턴은 그대로 확보.
     */
    @Provides
    @Singleton
    fun provideBankDatabase(@ApplicationContext context: Context): BankDatabase =
        Room.inMemoryDatabaseBuilder(context, BankDatabase::class.java).build()

    @Provides
    fun provideAccountDao(database: BankDatabase): AccountDao = database.accountDao()
}
