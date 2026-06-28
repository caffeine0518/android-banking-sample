package com.study.bank.data.repository.transaction

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.local.entity.TransactionEntity
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.repository.NoopKftcApiService
import com.study.bank.domain.model.account.AccountId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [TransactionRemoteMediator] 네트워크→DB 적재 검증.
 *
 * 메모리 커서(befor_inquiry_trace_info) 재사용, REFRESH 시 캐시 교체, next_page_yn→endOfPaginationReached
 * 매핑, 더 받을 게 없을 때 네트워크 미호출을 본다. Room 없이 인메모리 fake DAO로 돈다.
 */
@OptIn(ExperimentalPagingApi::class)
class TransactionRemoteMediatorTest {

    private val accountId = AccountId("120220112345678901234001")
    private val transactionDao = FakeTransactionDao()

    @Test
    fun `REFRESH는 첫 페이지를 적재하고 다음 페이지가 있으면 미완료를 알린다`() = runTest {
        val api = FakePagedApi(twoPageScript())
        val mediator = mediator(api)

        val result = mediator.load(LoadType.REFRESH, emptyState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, transactionDao.entities.size)
        assertNull("첫 페이지는 커서 없이 호출", api.lastRequestedCursor)
    }

    @Test
    fun `APPEND는 저장된 커서로 다음 페이지를 이어 적재하고 마지막이면 완료를 알린다`() = runTest {
        val api = FakePagedApi(twoPageScript())
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyState())
        val append = mediator.load(LoadType.APPEND, emptyState())

        assertTrue((append as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(4, transactionDao.entities.size)
        assertEquals("두 번째 호출은 1페이지가 준 커서로", "CURSOR-1", api.lastRequestedCursor)
    }

    @Test
    fun `더 받을 커서가 없으면 APPEND는 네트워크 없이 완료를 알린다`() = runTest {
        val api = FakePagedApi(twoPageScript())
        val mediator = mediator(api)
        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, emptyState()) // 끝까지 적재
        val callsBefore = api.callCount

        val again = mediator.load(LoadType.APPEND, emptyState())

        assertTrue((again as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals("네트워크를 더 호출하면 안 된다", callsBefore, api.callCount)
    }

    @Test
    fun `REFRESH는 직전 적재분을 교체하고 커서를 다시 첫 페이지로 되돌린다`() = runTest {
        val api = FakePagedApi(twoPageScript())
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyState())
        mediator.load(LoadType.APPEND, emptyState()) // 4건
        mediator.load(LoadType.REFRESH, emptyState()) // 다시 첫 페이지만

        assertEquals(2, transactionDao.entities.size)
    }

    @Test
    fun `PREPEND는 네트워크 없이 즉시 완료를 알린다`() = runTest {
        val api = FakePagedApi(twoPageScript())

        val result = mediator(api).load(LoadType.PREPEND, emptyState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(0, api.callCount)
    }

    // --- helpers ---

    private fun mediator(api: KftcApiService) = TransactionRemoteMediator(
        accountId = accountId,
        api = api,
        dtoMapper = TransactionMapper(),
        entityMapper = TransactionEntityMapper(),
        transactionDao = transactionDao,
        bankTranId = "M202300001U000001",
        fromDate = "20260101",
        toDate = "20261231",
        tranDtime = "20260603120000",
    )

    private fun emptyState() = PagingState<Int, TransactionEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    /** page1(커서 null)→2건+CURSOR-1+Y, page2(CURSOR-1)→2건+N. 거래 일시가 전부 달라 id가 유일하다. */
    private fun twoPageScript(): Map<String?, TransactionListResponse> = mapOf(
        null to pageResponse(timeBase = 100, nextPageYn = "Y", nextCursor = "CURSOR-1"),
        "CURSOR-1" to pageResponse(timeBase = 200, nextPageYn = "N", nextCursor = ""),
    )

    private fun pageResponse(timeBase: Int, nextPageYn: String, nextCursor: String) = TransactionListResponse(
        apiTranId = "T-$timeBase",
        apiTranDtm = "20260603120000000",
        rspCode = "A0000",
        rspMessage = "",
        bankTranId = "M202300001U000001",
        fintechUseNum = accountId.value,
        balanceAmt = "2847320",
        currencyCode = "KRW",
        resCnt = "2",
        resList = (0 until 2).map { offset ->
            TransactionItemDto(
                tranDate = "20260601",
                tranTime = "%06d".format(timeBase + offset),
                inoutType = "출금",
                tranType = "이체",
                printContent = "더미",
                tranAmt = "1000",
                afterBalanceAmt = "2847320",
            )
        },
        nextPageYn = nextPageYn,
        beforInquiryTraceInfo = nextCursor,
    )

    private class FakeTransactionDao : TransactionDao {
        val entities = mutableListOf<TransactionEntity>()
        override fun observeByAccountId(accountId: String): Flow<List<TransactionEntity>> =
            MutableStateFlow(entities.toList())
        override fun pagingSource(accountId: String): PagingSource<Int, TransactionEntity> = error("unused")
        override suspend fun insertAll(entities: List<TransactionEntity>) {
            val incoming = entities.map { it.id }.toSet()
            this.entities.removeAll { it.id in incoming }
            this.entities += entities
        }
        override suspend fun clearByAccountId(accountId: String) {
            entities.removeAll { it.accountId == accountId }
        }
    }

    private class FakePagedApi(
        private val pageByCursor: Map<String?, TransactionListResponse>,
    ) : KftcApiService by NoopKftcApiService {
        var callCount = 0
            private set
        var lastRequestedCursor: String? = null
            private set

        override suspend fun getTransactionList(
            bankTranId: String,
            fintechUseNum: String,
            fromDate: String,
            toDate: String,
            tranDtime: String,
            inquiryType: String,
            inquiryBase: String,
            sortOrder: String,
            beforInquiryTraceInfo: String?,
        ): TransactionListResponse {
            callCount++
            lastRequestedCursor = beforInquiryTraceInfo
            return pageByCursor[beforInquiryTraceInfo] ?: error("no page for cursor=$beforInquiryTraceInfo")
        }
    }
}
