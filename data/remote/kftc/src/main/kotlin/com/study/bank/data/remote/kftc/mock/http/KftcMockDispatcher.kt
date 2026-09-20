package com.study.bank.data.remote.kftc.mock.http

import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import com.study.bank.data.remote.kftc.mock.http.routing.Route
import com.study.bank.data.remote.kftc.mock.http.routing.RoutedRequest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy

/**
 * KFTC 오픈뱅킹 v2.0 mock 라우터.
 *
 * 책임은 연결 차단 토글 + [routes] 매칭뿐. 엔드포인트 선언은 [kftcRoutes], 엔드포인트별 로직은 각 핸들러가,
 * 라우팅 레벨 에러(잘못된 URL/미등록 경로/미지원 메서드)만 여기서 직접 응답한다.
 */
internal class KftcMockDispatcher(
    private val routes: List<Route>,
    private val errors: ErrorResponseMapper,
) : Dispatcher() {

    @Volatile
    var dropConnections: Boolean = false

    /** [dropConnections]는 응답만 유실시킨다 — [route]를 먼저 실행하므로 서버 상태는 이미 반영됐다. */
    override fun dispatch(request: RecordedRequest): MockResponse {
        val response = route(request)
        if (dropConnections) return MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST }
        return response
    }

    /**
     * 경로를 먼저 맞추고 그다음 메서드를 맞춘다 — 경로가 아예 없으면 404, 경로는 있는데 메서드가 다르면 405로
     * 갈라야 하기 때문. 한 번에 (메서드, 경로)로 찾으면 둘을 구분할 수 없다.
     */
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
