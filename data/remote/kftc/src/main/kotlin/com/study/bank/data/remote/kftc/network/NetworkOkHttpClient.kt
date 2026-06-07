package com.study.bank.data.remote.kftc.network

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Singleton
class NetworkOkHttpClient @Inject constructor() {
    val value: OkHttpClient = OkHttpClient.Builder().build()
}
