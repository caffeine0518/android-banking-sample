package com.study.bank.data.remote.kftc.mock.dispatcher

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

/**
 * KFTC 오픈뱅킹 v2.0 mock 라우터.
 *
 * 책임은 장애 토글 + path → 핸들러 분기뿐. 엔드포인트별 로직은 [accountHandler]/[transferHandler]가,
 * 라우팅 레벨 에러(잘못된 URL/미등록 path/장애 주입)만 [responses]가 직접 응답한다.
 * 협력자는 모두 [com.study.bank.data.remote.kftc.mock.KftcMockServer]가 구성해 주입한다.
 */
internal class KftcMockDispatcher(
    private val accountHandler: AccountRequestHandler,
    private val transferHandler: TransferRequestHandler,
    private val responses: KftcMockResponses,
) : Dispatcher() {

    /**
     * 테스트 전용 장애 주입 스위치. true면 모든 요청에 [MockError.ServerFault](5xx)를 응답해
     * "새로고침 실패 → 에러 스낵바" 경로를 E2E에서 재현한다. 여러 스레드(메인/네트워크)가 읽으므로 @Volatile.
     */
    @Volatile
    var faultEnabled: Boolean = false

    override fun dispatch(request: RecordedRequest): MockResponse {
        if (faultEnabled) return responses.error(MockError.ServerFault)

        val url = request.requestUrl ?: return responses.error(MockError.InvalidUrl)

        return when (url.encodedPath) {
            PATH_LIST_FINUSE -> accountHandler.list()
            PATH_BALANCE_FIN_NUM -> accountHandler.balance(url.queryParameter(QUERY_FINTECH_USE_NUM))
            PATH_TRANSACTION_LIST_FIN_NUM ->
                accountHandler.transactionList(url.queryParameter(QUERY_FINTECH_USE_NUM))
            // peek()로 본문을 복사 읽어 takeRequest()의 RecordedRequest body를 소비하지 않는다.
            PATH_TRANSFER_WITHDRAW_FIN_NUM -> transferHandler.withdraw(request.body.peek().readUtf8())
            else -> responses.error(MockError.UnknownEndpoint(url.encodedPath))
        }
    }
}
