package com.study.bank.data.remote.kftc.api

import com.study.bank.data.remote.kftc.network.KftcRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KftcApiServiceImpl @Inject constructor(
    retrofit: KftcRetrofit,
) : KftcApiService by retrofit.value.create(KftcApiService::class.java)
