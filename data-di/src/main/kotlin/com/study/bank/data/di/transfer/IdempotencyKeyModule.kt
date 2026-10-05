package com.study.bank.data.di.transfer

import com.study.bank.domain.model.transfer.IdempotencyKeyGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID

/** E2E 가 키를 지정하려고 교체하므로 public 이다. */
@Module
@InstallIn(SingletonComponent::class)
object IdempotencyKeyModule {

    @Provides
    fun provideIdempotencyKeyGenerator(): IdempotencyKeyGenerator =
        IdempotencyKeyGenerator { UUID.randomUUID().toString() }
}
