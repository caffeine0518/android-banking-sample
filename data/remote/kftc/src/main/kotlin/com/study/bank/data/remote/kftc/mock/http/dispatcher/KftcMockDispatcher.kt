package com.study.bank.data.remote.kftc.mock.http.dispatcher

import com.study.bank.data.remote.kftc.mock.http.response.MockError
import com.study.bank.data.remote.kftc.mock.http.routing.Route
import com.study.bank.data.remote.kftc.mock.http.routing.RoutedRequest
import com.study.bank.data.remote.kftc.mock.mapper.ErrorResponseMapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy

/**
 * KFTC 오픈뱅킹 v2.0 mock 라우터.
 *
 * 연결 차단과 [routes] 매칭만 담당한다. 엔드포인트 선언은 `kftcRoutes`에, 엔드포인트별 로직은 각 핸들러에 있다.
 * 라우팅 단계의 오류(잘못된 URL, 미등록 경로, 미지원 메서드)만 여기서 응답한다.
 */
internal class KftcMockDispatcher(
    private val routes: List<Route>,
    private val errors: ErrorResponseMapper,
) : Dispatcher() {

    @Volatile
    var dropConnections: Boolean = false

    /** [dropConnections]는 응답만 유실시킨다. [route]를 먼저 실행하므로 서버 상태는 이미 반영된 뒤다. */
    override fun dispatch(request: RecordedRequest): MockResponse {
        val response = route(request)
        if (dropConnections) return MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST }
        return response
    }

    /**
     * 경로를 먼저 찾고 그다음 메서드를 확인한다. 경로가 없으면 404, 경로는 있지만 메서드가 다르면 405로
     * 응답해야 하는데, (메서드, 경로)로 한 번에 찾으면 둘을 구분할 수 없다.
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
