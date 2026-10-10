package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.HttpUrl
import okhttp3.mockwebserver.RecordedRequest

internal class RoutedRequest(private val request: RecordedRequest) {

    val url: HttpUrl get() = checkNotNull(request.requestUrl)

    fun query(name: String): String? = url.queryParameter(name)

    /** peek()로 읽어 takeRequest()가 반환하는 요청의 body를 소비하지 않는다. */
    fun body(): String = request.body.peek().readUtf8()
}
