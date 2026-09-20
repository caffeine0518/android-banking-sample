package com.study.bank.data.remote.kftc.mock.http.handler

import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.mock.http.MockError
import com.study.bank.data.remote.kftc.mock.http.response.KftcMockResponses
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import okhttp3.mockwebserver.MockResponse

/**
 * KFTC `/v2.0/account/…` 조회 핸들러.
 *
 * 조회 전용이라 쓰기 규칙([KftcWithdrawalService])을 거치지 않고 DAO를 직접 읽는다.
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
        const val PAGE_SIZE = KFTC_TRANSACTION_PAGE_SIZE

        // 클라는 받은 커서를 그대로 되돌려보내기만 한다(불투명 토큰).
        private const val CURSOR_PREFIX = "INQ"

        fun encodeCursor(seq: Long): String = "$CURSOR_PREFIX$seq"

        fun decodeCursor(token: String?): Long? =
            token?.removePrefix(CURSOR_PREFIX)?.toLongOrNull()
    }
}
