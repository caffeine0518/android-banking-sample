package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.dispatcher.KftcMockDispatcher
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.TimeUnit

/**
 * KFTC v2.0 mock 서버 라이프사이클 래퍼.
 *
 * 호스트 앱에서는 프로세스 시작 시 [start]로 띄워두고 [baseUrl]을 Retrofit baseUrl로 주입한다.
 * [com.study.bank.data.remote.kftc.api.KftcApiService]가 실제 네트워크 스택을 그대로 타게 되어
 * 인터셉터/직렬화/에러 경로까지 in-process로 검증할 수 있다.
 */
class KftcMockServer {

    private val server: MockWebServer = MockWebServer()
    private val dispatcher = KftcMockDispatcher()
    private var started: Boolean = false

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
