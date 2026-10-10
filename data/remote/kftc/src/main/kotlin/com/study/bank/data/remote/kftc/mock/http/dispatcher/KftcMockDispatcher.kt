package com.study.bank.data.remote.kftc.mock.http.dispatcher

import com.study.bank.data.remote.kftc.mock.http.response.MockError
import com.study.bank.data.remote.kftc.mock.http.routing.Route
import com.study.bank.data.remote.kftc.mock.http.routing.RoutedRequest
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy

internal class KftcMockDispatcher(
    private val routes: List<Route>,
    private val errors: ErrorResponseMapper,
) : Dispatcher() {

    @Volatile
    var dropConnections: Boolean = false

    /** [route]를 먼저 실행하므로 [dropConnections]여도 서버 상태는 반영된다. */
    override fun dispatch(request: RecordedRequest): MockResponse {
        val response = route(request)
        if (dropConnections) return MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST }
        return response
    }

    /** 404(경로 없음)와 405(메서드 불일치)를 구분하려고 경로를 먼저 찾는다. */
    private fun route(request: RecordedRequest): MockResponse {
        val url = request.requestUrl ?: return errors.toResponse(MockError.InvalidUrl)
        val path = url.encodedPath
        val samePath = routes.filter { it.path == path }
        if (samePath.isEmpty()) return errors.toResponse(MockError.UnknownEndpoint(path))

        val method = request.method.orEmpty()
        val route = samePath.firstOrNull { it.method == method }
            ?: return errors.toResponse(MockError.MethodNotAllowed(method, path))
        return route.handle(RoutedRequest(request))
    }
}
