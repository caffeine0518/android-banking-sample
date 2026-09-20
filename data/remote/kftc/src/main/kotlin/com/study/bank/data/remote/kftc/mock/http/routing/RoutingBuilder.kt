package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.mockwebserver.MockResponse

/** [routing] DSL의 수신 객체. 등록 순서대로 [Route] 목록을 모은다. */
internal class RoutingBuilder {

    private val routes = mutableListOf<Route>()

    fun get(path: String, handle: (RoutedRequest) -> MockResponse) {
        routes += Route(METHOD_GET, path, handle)
    }

    fun post(path: String, handle: (RoutedRequest) -> MockResponse) {
        routes += Route(METHOD_POST, path, handle)
    }

    internal fun build(): List<Route> = routes.toList()

    private companion object {
        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
    }
}

/** mock 서버가 노출하는 엔드포인트 전체를 한 곳에 선언한다. */
internal fun routing(block: RoutingBuilder.() -> Unit): List<Route> =
    RoutingBuilder().apply(block).build()
