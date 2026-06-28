package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.mock.KftcBankState
import com.study.bank.data.remote.kftc.mock.SeedAccount
import com.study.bank.data.remote.kftc.mock.TransactionRecord
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

    /**
     * 거래내역 한 페이지(KFTC 연속조회). 전체 명세서(시드 과거 + 세션 이체)에서 [beforInquiryTraceInfo](커서)가
     * 가리키는 지점부터 서버가 정한 [PAGE_SIZE]건을 돌려준다. 커서가 없으면 첫 페이지.
     * next_page_yn과 다음 커서(befor_inquiry_trace_info)를 함께 실어 클라가 연속조회를 이어가게 한다.
     */
    fun transactionList(fintechUseNum: String?, beforInquiryTraceInfo: String?): MockResponse {
        val account = resolveAccount(fintechUseNum) ?: return missingOrUnknown(fintechUseNum)
        val page = pageFrom(state.statement(account.fintechUseNum), beforInquiryTraceInfo)
        return responses.transactionList(account, page.records, page.hasNext, page.nextCursor)
    }

    /**
     * 명세서(seq 내림차순)에서 [cursor]가 가리키는 seq보다 **더 작은(오래된)** 쪽으로 [PAGE_SIZE]건을 잘라 한 페이지로 만든다.
     * 커서가 없으면 첫 페이지. 끝이면 hasNext=false·빈 커서.
     *
     * 키셋(seek) 방식 — 커서는 오프셋이 아니라 마지막 행의 seq(단조 증가 고유값)다. 그래서 (1) 페이지 도중 새 거래가
     * 머리에 끼어도 경계가 밀리지 않고, (2) seq가 유일하므로 strict `<`로 같은 초 행도 누락 없이 seek한다.
     */
    private fun pageFrom(statement: List<TransactionRecord>, cursor: String?): TransactionPage {
        val afterSeq = decodeCursor(cursor)
        val remaining = if (afterSeq == null) statement else statement.filter { it.seq < afterSeq }
        val records = remaining.take(PAGE_SIZE)
        val hasNext = remaining.size > PAGE_SIZE
        return TransactionPage(
            records = records,
            hasNext = hasNext,
            nextCursor = if (hasNext) encodeCursor(records.last().seq) else "",
        )
    }

    /** transaction_list 한 페이지: 잘라낸 거래 + 다음 페이지 여부와 다음 커서. */
    private data class TransactionPage(
        val records: List<TransactionRecord>,
        val hasNext: Boolean,
        val nextCursor: String,
    )

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
