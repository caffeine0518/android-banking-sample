package com.study.bank.data.di.local

import android.content.Context
import androidx.room.Room
import com.study.bank.data.local.BankDatabase
import com.study.bank.data.local.dao.AccountDao
import com.study.bank.data.local.dao.TransactionDao
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
     * KFTC mock은 프로세스가 재시작되면 시드로 초기화되므로, 캐시도 인메모리로 두어 mock과 수명을 맞춘다.
     */
    @Provides
    @Singleton
    fun provideBankDatabase(@ApplicationContext context: Context): BankDatabase =
        Room.inMemoryDatabaseBuilder(context, BankDatabase::class.java).build()

    @Provides
    fun provideAccountDao(database: BankDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: BankDatabase): TransactionDao = database.transactionDao()
}
