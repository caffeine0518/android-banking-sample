package com.study.bank.data.remote.kftc.mock.dispatcher

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy

/**
 * KFTC 오픈뱅킹 v2.0 mock 라우터.
 *
 * 책임은 연결 차단 토글 + path → 핸들러 분기뿐. 엔드포인트별 로직은 [accountHandler]/[transferHandler]가,
 * 라우팅 레벨 에러(잘못된 URL/미등록 path)만 [responses]가 직접 응답한다.
 * 협력자는 모두 [com.study.bank.data.remote.kftc.mock.KftcMockServer]가 구성해 주입한다.
 */
internal class KftcMockDispatcher(
    private val accountHandler: AccountRequestHandler,
    private val transferHandler: TransferRequestHandler,
    private val inquiryHandler: InquiryRequestHandler,
    private val responses: KftcMockResponses,
) : Dispatcher() {

    @Volatile
    var dropConnections: Boolean = false

    override fun dispatch(request: RecordedRequest): MockResponse {
        // dispatch는 요청을 읽은 뒤라 AT_START(읽기 전 차단)가 아니라 AFTER_REQUEST다.
        if (dropConnections) return MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST }

        val url = request.requestUrl ?: return responses.error(MockError.InvalidUrl)

        return when (url.encodedPath) {
            PATH_LIST_FINUSE -> accountHandler.list()
            PATH_BALANCE_FIN_NUM -> accountHandler.balance(url.queryParameter(QUERY_FINTECH_USE_NUM))
            PATH_TRANSACTION_LIST_FIN_NUM -> accountHandler.transactionList(
                fintechUseNum = url.queryParameter(QUERY_FINTECH_USE_NUM),
                beforInquiryTraceInfo = url.queryParameter(QUERY_BEFOR_INQUIRY_TRACE_INFO),
            )
            // peek()로 본문을 복사 읽어 takeRequest()의 RecordedRequest body를 소비하지 않는다.
            PATH_TRANSFER_WITHDRAW_FIN_NUM -> transferHandler.withdraw(request.body.peek().readUtf8())
            PATH_INQUIRY_REAL_NAME -> inquiryHandler.realName(request.body.peek().readUtf8())
            else -> responses.error(MockError.UnknownEndpoint(url.encodedPath))
        }
    }
}
