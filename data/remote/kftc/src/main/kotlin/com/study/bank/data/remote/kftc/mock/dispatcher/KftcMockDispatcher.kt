package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.mock.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.SeedAccount
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

/**
 * KFTC 오픈뱅킹 v2.0 mock 라우터.
 *
 * 책임은 path → 핸들러 분기뿐. envelope 채움/직렬화/MockResponse 조립은 [KftcMockResponses],
 * 시드→DTO 변환은 [SeedAccountMappers]에 위임한다.
 */
internal class KftcMockDispatcher(
    private val seed: List<SeedAccount> = KftcAccountSeed.accounts,
    private val responses: KftcMockResponses = KftcMockResponses(),
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
            PATH_LIST_FINUSE -> responses.listFinuse(seed)
            PATH_BALANCE_FIN_NUM -> handleBalance(url.queryParameter(QUERY_FINTECH_USE_NUM))
            else -> responses.error(MockError.UnknownEndpoint(url.encodedPath))
        }
    }

    private fun handleBalance(fintechUseNum: String?): MockResponse {
        if (fintechUseNum.isNullOrBlank()) {
            return responses.error(MockError.MissingFintechUseNum)
        }
        val account = seed.firstOrNull { it.fintechUseNum == fintechUseNum }
            ?: return responses.error(MockError.UnknownFintechUseNum(fintechUseNum))
        return responses.balanceFinNum(account)
    }
}
