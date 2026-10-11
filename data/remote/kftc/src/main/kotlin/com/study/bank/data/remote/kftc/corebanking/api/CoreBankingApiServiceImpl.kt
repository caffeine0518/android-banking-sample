package com.study.bank.data.remote.kftc.corebanking.api

import com.study.bank.data.remote.kftc.network.KftcRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreBankingApiServiceImpl @Inject constructor(
    retrofit: KftcRetrofit,
) : CoreBankingApiService by retrofit.value.create(CoreBankingApiService::class.java)
