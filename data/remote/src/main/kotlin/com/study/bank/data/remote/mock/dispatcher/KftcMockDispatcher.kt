package com.study.bank.data.remote.mock.dispatcher

import com.study.bank.data.remote.mock.KftcAccountSeed
import com.study.bank.data.remote.mock.SeedAccount
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

    override fun dispatch(request: RecordedRequest): MockResponse {
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
