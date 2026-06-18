package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.mock.KftcBankState
import com.study.bank.data.remote.kftc.mock.SeedAccount
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/account/…` 읽기 요청 핸들러: 계좌목록/잔액/거래내역.
 *
 * 세 엔드포인트가 공유하는 계좌 조회(fintech_use_num null/blank → 누락 400, 미존재 → 404)를 한곳에 둔다.
 * 라우팅은 [KftcMockDispatcher], 상태는 [state], 응답 조립은 [responses]에 위임한다.
 */
internal class AccountRequestHandler(
    private val state: KftcBankState,
    private val responses: KftcMockResponses,
) {
    fun list(): MockResponse = responses.listFinuse(state.accounts())

    fun balance(fintechUseNum: String?): MockResponse {
        val account = resolveAccount(fintechUseNum) ?: return missingOrUnknown(fintechUseNum)
        return responses.balanceFinNum(account)
    }

    fun transactionList(fintechUseNum: String?): MockResponse {
        val account = resolveAccount(fintechUseNum) ?: return missingOrUnknown(fintechUseNum)
        return responses.transactionList(account, state.transactions(fintechUseNum!!))
    }

    private fun resolveAccount(fintechUseNum: String?): SeedAccount? {
        if (fintechUseNum.isNullOrBlank()) return null
        return state.account(fintechUseNum)
    }

    private fun missingOrUnknown(fintechUseNum: String?): MockResponse =
        if (fintechUseNum.isNullOrBlank()) {
            responses.error(MockError.MissingFintechUseNum)
        } else {
            responses.error(MockError.UnknownFintechUseNum(fintechUseNum))
        }
}
