package com.study.bank.data.remote.kftc.mock.http.routing

import okhttp3.mockwebserver.MockResponse

internal class Route(
    val method: String,
    val path: String,
    val handle: (RoutedRequest) -> MockResponse,
)
