package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.kftcRoutes
import com.study.bank.data.remote.kftc.mock.http.response.KftcMockResponses
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/account/…` 읽기 요청 핸들러: 계좌목록/잔액/거래내역.
 *
 * 세 엔드포인트가 공유하는 계좌 조회(fintech_use_num null/blank → 누락 400, 미존재 → 404)를 한곳에 둔다.
 * 조회 전용이라 쓰기 규칙(KftcWithdrawalService)을 거치지 않고 DAO를 직접 읽는다.
 * 라우팅은 [kftcRoutes],
 * 응답 조립은 [responses]에 위임한다.
 */
internal class AccountRequestHandler(
    private val accountDao: MockAccountDao,
    private val transactionDao: MockTransactionDao,
    private val responses: KftcMockResponses,
) {
    fun list(): MockResponse = responses.listFinuse(accountDao.findAll())

    fun balance(fintechUseNum: String?): MockResponse {
        val account = resolveAccount(fintechUseNum) ?: return missingOrUnknown(fintechUseNum)
        return responses.balanceFinNum(account)
    }

    /**
     * 거래내역 한 페이지(KFTC 연속조회). [beforInquiryTraceInfo](커서)가 가리키는 지점부터 [PAGE_SIZE]건을
     * 반환한다. 커서가 없으면 첫 페이지. next_page_yn과 다음 커서를 함께 실어 클라가 연속조회를 이어가게 한다.
     *
     * 다음 페이지 유무는 **한 건 더 조회해** 판정한다 — 별도 count 질의 없이 경계를 알 수 있다.
     */
    fun transactionList(fintechUseNum: String?, beforInquiryTraceInfo: String?): MockResponse {
        val account = resolveAccount(fintechUseNum) ?: return missingOrUnknown(fintechUseNum)
        val fetched = transactionDao.page(
            fintechUseNum = account.fintechUseNum,
            afterSeq = decodeCursor(beforInquiryTraceInfo),
            limit = PAGE_SIZE + 1,
        )
        val records = fetched.take(PAGE_SIZE)
        val hasNext = fetched.size > PAGE_SIZE
        return responses.transactionList(
            account = account,
            records = records,
            hasNext = hasNext,
            nextCursor = if (hasNext) encodeCursor(records.last().seq) else "",
        )
    }

    private fun resolveAccount(fintechUseNum: String?): SeedAccount? {
        if (fintechUseNum.isNullOrBlank()) return null
        return accountDao.find(fintechUseNum)
    }

    private fun missingOrUnknown(fintechUseNum: String?): MockResponse =
        if (fintechUseNum.isNullOrBlank()) {
            responses.error(MockError.MissingFintechUseNum)
        } else {
            responses.error(MockError.UnknownFintechUseNum(fintechUseNum))
        }

    private companion object {
        // 서버가 정하는 페이지 크기(단일 소유처). 1,200건 시드면 60페이지.
        const val PAGE_SIZE = KFTC_TRANSACTION_PAGE_SIZE

        // 연속조회 커서. 마지막 행의 seq를 불투명 토큰으로 감싼다(클라는 그대로 되돌려보내기만 함).
        private const val CURSOR_PREFIX = "INQ"

        fun encodeCursor(seq: Long): String = "$CURSOR_PREFIX$seq"

        fun decodeCursor(token: String?): Long? =
            token?.removePrefix(CURSOR_PREFIX)?.toLongOrNull()
    }
}
