package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.dispatcher.KftcMockDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.TimeUnit

/**
 * Lifecycle wrapper for the KFTC v2.0 mock server.
 *
 * Lets [com.study.bank.data.remote.kftc.api.KftcApiService] exercise the real network stack so
 * interceptors, serialization, and error paths are all verified in-process.
 */
@Singleton
class KftcMockServer @Inject constructor() {

    private val server: MockWebServer = MockWebServer()
    private val dispatcher = KftcMockDispatcher()
    private var started: Boolean = false

    init {
        start()
    }

    fun start() {
        if (started) return
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
