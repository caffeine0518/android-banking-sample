package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.mockwebserver.MockResponse

/**
 * 라우트 하나 — (HTTP 메서드, 경로) 짝에 핸들러를 연결한다.
 *
 * 메서드가 매칭 키에 포함되므로 `POST /v2.0/account/list_finuse` 같은 요청이 조회 핸들러로 가지 않는다.
 */
internal class Route(
    val method: String,
    val path: String,
    val handle: (RoutedRequest) -> MockResponse,
)
