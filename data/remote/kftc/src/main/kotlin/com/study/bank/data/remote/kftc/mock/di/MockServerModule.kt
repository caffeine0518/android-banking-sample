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

/** mock 내부 구현체 바인딩. 구현체가 internal이라 `:data-di`가 아니라 이 모듈 안에 둔다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MockServerModule {

    @Binds
    @Singleton
    internal abstract fun bindKftcMockServer(impl: KftcMockServerImpl): KftcMockServer

    /** 상태를 원장(Room)에만 두므로 스코프 없이 바인딩한다. */
    @Binds
    internal abstract fun bindKftcWithdrawalService(
        impl: KftcWithdrawalServiceImpl,
    ): KftcWithdrawalService

    companion object {

        /** 연결 차단 토글과 api_tran_id 시퀀스를 서버 하나가 공유하도록 [Singleton]이어야 한다. */
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

        /** 데모/수동 테스트용: 송금 응답을 지연시켜 "보내는 중이에요" 로딩 화면이 최소 1초 보이게 한다. */
        private const val WITHDRAW_RESPONSE_DELAY_MS = 1_000L
    }
}
