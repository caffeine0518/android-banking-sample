package com.study.bank.data.remote.kftc.network

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Singleton
class NetworkOkHttpClient @Inject constructor(
    mockServer: KftcMockServer,
) {
    val value: OkHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(
            mockServer.clientCertificates.sslSocketFactory(),
            mockServer.clientCertificates.trustManager,
        )
        .build()
}
