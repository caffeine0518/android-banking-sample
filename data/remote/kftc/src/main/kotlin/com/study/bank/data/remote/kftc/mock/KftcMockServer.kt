package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.dispatcher.KftcMockDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.util.concurrent.TimeUnit

/**
 * Lifecycle wrapper for the KFTC v2.0 mock server.
 *
 * Lets [com.study.bank.data.remote.kftc.api.KftcApiService] exercise the real network stack so
 * interceptors, serialization, and error paths are all verified in-process. Serves HTTPS with a
 * self-signed loopback certificate so the manifest stays cleartext-free; clients use
 * [clientCertificates] to trust this CA.
 */
@Singleton
class KftcMockServer @Inject constructor() {

    private val server: MockWebServer = MockWebServer()
    private val dispatcher = KftcMockDispatcher()
    private val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    val clientCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localhostCertificate.certificate)
        .build()

    private var started: Boolean = false

    init {
        start()
    }

    fun start() {
        if (started) return
        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        server.useHttps(serverCertificates.sslSocketFactory(), false)
        server.dispatcher = dispatcher
        server.start()
        started = true
    }

    fun baseUrl(): HttpUrl {
        check(started) { "KftcMockServer가 아직 start되지 않았다" }
        return server.url("/")
    }

    fun shutdown() {
        if (!started) return
        server.shutdown()
        started = false
    }

    /** 테스트 전용: 가장 오래된 수신 요청 1건을 큐에서 꺼낸다. */
    internal fun takeRequest(timeoutMs: Long = 1_000): RecordedRequest? =
        server.takeRequest(timeoutMs, TimeUnit.MILLISECONDS)
}
