package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.network.KeximRetrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeximHttpApiImpl @Inject constructor(
    retrofit: KeximRetrofit,
) : KeximHttpApi by retrofit.value.create(KeximHttpApi::class.java)
