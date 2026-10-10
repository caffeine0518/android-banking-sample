package com.study.bank.data.remote.kftc.mock.di

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.mock.KftcMockServerImpl
import com.study.bank.data.remote.kftc.mock.http.dispatcher.KftcMockDispatcher
import com.study.bank.data.remote.kftc.mock.http.dispatcher.kftcMockDispatcher
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalServiceImpl
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.network.NetworkJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockServerModule {

    @Binds
    @Singleton
    internal abstract fun bindKftcMockServer(impl: KftcMockServerImpl): KftcMockServer

    @Binds
    internal abstract fun bindKftcWithdrawalService(
        impl: KftcWithdrawalServiceImpl,
    ): KftcWithdrawalService

    companion object {

        /** 연결 차단 플래그와 api_tran_id 시퀀스가 하나여야 하므로 [Singleton]이다. */
        @Provides
        @Singleton
        fun provideKftcMockDispatcher(
            accountDao: MockAccountDao,
            transactionDao: MockTransactionDao,
            withdrawalService: KftcWithdrawalService,
            accountSeed: List<SeedAccount>,
            networkJson: NetworkJson,
        ): KftcMockDispatcher = kftcMockDispatcher(
            accountDao = accountDao,
            transactionDao = transactionDao,
            withdrawalService = withdrawalService,
            accountSeed = accountSeed,
            json = networkJson.value,
            responseDelayMillis = WITHDRAW_RESPONSE_DELAY_MS,
        )

        /** 송금 로딩 화면이 최소 1초 보이도록 지연한다. */
        private const val WITHDRAW_RESPONSE_DELAY_MS = 1_000L
    }
}
