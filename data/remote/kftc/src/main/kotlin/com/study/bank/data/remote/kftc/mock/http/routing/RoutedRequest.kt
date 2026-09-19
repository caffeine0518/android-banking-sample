package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.HttpUrl
import okhttp3.mockwebserver.RecordedRequest

/** 라우트 핸들러가 받는 요청 — 쿼리와 본문을 꺼내는 통로만 노출한다. */
internal class RoutedRequest(private val request: RecordedRequest) {

    val url: HttpUrl get() = checkNotNull(request.requestUrl)

    fun query(name: String): String? = url.queryParameter(name)

    /** peek()로 본문을 복사 읽어 takeRequest()의 RecordedRequest body를 소비하지 않는다. */
    fun body(): String = request.body.peek().readUtf8()
}
