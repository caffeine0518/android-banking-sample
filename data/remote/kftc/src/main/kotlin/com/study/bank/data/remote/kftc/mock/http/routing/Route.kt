package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.mockwebserver.MockResponse

/**
 * (HTTP 메서드, 경로) 쌍을 핸들러에 연결하는 라우트.
 *
 * 메서드도 매칭 조건이므로 `POST /v2.0/account/list_finuse` 같은 요청은 조회 핸들러로 전달되지 않는다.
 */
internal class Route(
    val method: String,
    val path: String,
    val handle: (RoutedRequest) -> MockResponse,
)
