package com.study.bank.data.repository.transaction

import com.study.bank.data.local.dao.TransactionDao
import com.study.bank.data.local.entity.TransactionEntity
import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * [TransactionRepositoryImpl] SSOT 동작 검증.
 *
 * Room을 단위 테스트에서 띄우려면 Robolectric이 필요해 [TransactionDao]를 인터페이스 충실 모사로 대체
 * ([AccountRepositoryImplTest]와 동일 전략). 읽기는 DAO Flow만, refresh는 원격→DAO 통째 교체임을 본다.
 */
class TransactionRepositoryImplTest {

    private val salary = AccountId("120220112345678901234001")
    private val usd = AccountId("120220112345678901234002")

    @Test
    fun `refresh는 원격 거래내역을 매핑해 SSOT에 저장하고 observe로 흘린다`() = runTest {
        val api = FakeKftcApiService(
            mapOf(
                salary.value to txnResponse(
                    salary.value, "KRW", "2797320",
                    listOf(txnItem("20260618", "103000", "출금", print = "세이프박스로", amt = "50000", after = "2797320")),
                ),
            ),
        )
        val repo = buildRepo(api, FakeTransactionDao())

        repo.refresh(salary)

        val txns = repo.observeTransactions(salary).first()
        assertEquals(1, txns.size)
        val t = txns.first()
        assertEquals(salary, t.accountId)
        assertEquals(TransactionType.TRANSFER_OUT, t.type)
        assertEquals(Currency.KRW, t.amount.currency)
        assertEquals(0, t.amount.amount.compareTo(BigDecimal("50000")))
        assertEquals(0, t.balanceAfter.amount.compareTo(BigDecimal("2797320")))
        assertEquals("세이프박스로", t.counterparty?.name)
    }

    @Test
    fun `USD 계좌 거래 금액은 응답 통화로 매핑된다`() = runTest {
        val api = FakeKftcApiService(
            mapOf(
                usd.value to txnResponse(
                    usd.value, "USD", "3145.80",
                    listOf(txnItem("20260618", "103000", "출금", amt = "100.00", after = "3145.80")),
                ),
            ),
        )
        val repo = buildRepo(api, FakeTransactionDao())

        repo.refresh(usd)

        val t = repo.observeTransactions(usd).first().first()
        assertEquals(Currency.USD, t.amount.currency)
        assertEquals(0, t.amount.amount.compareTo(BigDecimal("100.00")))
    }

    @Test
    fun `refresh는 해당 계좌 내역을 통째 교체해 stale을 제거한다`() = runTest {
        val api = SequencedKftcApiService(
            mutableListOf(
                txnResponse(
                    salary.value, "KRW", "100",
                    listOf(
                        txnItem("20260618", "103000", "출금", amt = "10", after = "100"),
                        txnItem("20260617", "103000", "출금", amt = "20", after = "110"),
                    ),
                ),
                txnResponse(
                    salary.value, "KRW", "90",
                    listOf(txnItem("20260619", "103000", "출금", amt = "5", after = "90")),
                ),
            ),
        )
        val repo = buildRepo(api, FakeTransactionDao())

        repo.refresh(salary)
        assertEquals(2, repo.observeTransactions(salary).first().size)

        repo.refresh(salary)
        assertEquals(1, repo.observeTransactions(salary).first().size)
    }

    @Test
    fun `observeTransactions는 다른 계좌 내역을 섞지 않는다`() = runTest {
        val api = FakeKftcApiService(
            mapOf(
                salary.value to txnResponse(
                    salary.value, "KRW", "100",
                    listOf(txnItem("20260618", "103000", "출금", amt = "10", after = "100")),
                ),
                usd.value to txnResponse(
                    usd.value, "USD", "3000.00",
                    listOf(
                        txnItem("20260618", "103000", "출금", amt = "1.00", after = "3000.00"),
                        txnItem("20260617", "103000", "출금", amt = "2.00", after = "3001.00"),
                    ),
                ),
            ),
        )
        val repo = buildRepo(api, FakeTransactionDao())

        repo.refresh(salary)
        repo.refresh(usd)

        assertEquals(1, repo.observeTransactions(salary).first().size)
        assertEquals(2, repo.observeTransactions(usd).first().size)
        assertTrue(repo.observeTransactions(salary).first().all { it.accountId == salary })
    }

    @Test
    fun `observeTransactions는 최신 거래를 먼저 정렬한다`() = runTest {
        val api = FakeKftcApiService(
            mapOf(
                salary.value to txnResponse(
                    salary.value, "KRW", "0",
                    listOf(
                        txnItem("20260618", "120000", "출금", amt = "10", after = "30"), // 최신
                        txnItem("20260615", "090000", "출금", amt = "20", after = "40"), // 과거
                    ),
                ),
            ),
        )
        val repo = buildRepo(api, FakeTransactionDao())

        repo.refresh(salary)

        val txns = repo.observeTransactions(salary).first()
        assertEquals(2, txns.size)
        assertTrue("최신 거래가 먼저 와야 한다", txns[0].occurredAt.isAfter(txns[1].occurredAt))
    }

    private fun buildRepo(api: KftcApiService, dao: TransactionDao) = TransactionRepositoryImpl(
        api = api,
        dao = dao,
        dtoMapper = TransactionMapper(),
        entityMapper = TransactionEntityMapper(),
    )

    // --- fixtures ---

    private fun txnItem(
        date: String,
        time: String,
        inout: String,
        tranType: String = "이체",
        print: String = "상대방",
        amt: String,
        after: String,
    ) = TransactionItemDto(
        tranDate = date,
        tranTime = time,
        inoutType = inout,
        tranType = tranType,
        printContent = print,
        tranAmt = amt,
        afterBalanceAmt = after,
    )

    private fun txnResponse(
        fintechUseNum: String,
        currency: String,
        balance: String,
        items: List<TransactionItemDto>,
    ) = TransactionListResponse(
        apiTranId = "T0000000000000001",
        apiTranDtm = "20260618103000000",
        rspCode = "A0000",
        rspMessage = "",
        bankTranId = "M202300001U000001",
        fintechUseNum = fintechUseNum,
        balanceAmt = balance,
        currencyCode = currency,
        resCnt = items.size.toString(),
        resList = items,
    )

    // --- fakes ---

    /**
     * Room InvalidationTracker/Transaction 보장은 못 살리지만 SSOT의 행동(계좌별 필터, occurred_at desc 정렬,
     * replaceForAccount의 clear→insert)은 동일하게 모사한다. replaceForAccount는 인터페이스 기본 구현 사용.
     */
    private class FakeTransactionDao : TransactionDao {
        private val source = MutableStateFlow<List<TransactionEntity>>(emptyList())

        override fun observeByAccountId(accountId: String): Flow<List<TransactionEntity>> =
            source.map { all ->
                all.filter { it.accountId == accountId }
                    .sortedWith(compareByDescending<TransactionEntity> { it.occurredAt }.thenBy { it.id })
            }

        override suspend fun insertAll(entities: List<TransactionEntity>) {
            val incoming = entities.map { it.id }.toSet()
            source.value = source.value.filterNot { it.id in incoming } + entities
        }

        override suspend fun clearByAccountId(accountId: String) {
            source.value = source.value.filterNot { it.accountId == accountId }
        }
    }

    private class FakeKftcApiService(
        private val responses: Map<String, TransactionListResponse>,
    ) : KftcApiService by UnusedKftcApiService {
        override suspend fun getTransactionList(
            bankTranId: String,
            fintechUseNum: String,
            fromDate: String,
            toDate: String,
            tranDtime: String,
            inquiryType: String,
            inquiryBase: String,
            sortOrder: String,
        ): TransactionListResponse = responses[fintechUseNum] ?: error("no stub for $fintechUseNum")
    }

    private class SequencedKftcApiService(
        private val sequence: MutableList<TransactionListResponse>,
    ) : KftcApiService by UnusedKftcApiService {
        override suspend fun getTransactionList(
            bankTranId: String,
            fintechUseNum: String,
            fromDate: String,
            toDate: String,
            tranDtime: String,
            inquiryType: String,
            inquiryBase: String,
            sortOrder: String,
        ): TransactionListResponse = sequence.removeAt(0)
    }

    /** 본 테스트가 쓰지 않는 엔드포인트는 호출되면 실패하도록. */
    private object UnusedKftcApiService : KftcApiService {
        override suspend fun getAccountList(userSeqNo: String, includeCancelYn: String, sortOrder: String): AccountListResponse =
            error("unused")
        override suspend fun getAccountBalance(bankTranId: String, fintechUseNum: String, tranDtime: String): AccountBalanceResponse =
            error("unused")
        override suspend fun getTransactionList(
            bankTranId: String,
            fintechUseNum: String,
            fromDate: String,
            toDate: String,
            tranDtime: String,
            inquiryType: String,
            inquiryBase: String,
            sortOrder: String,
        ): TransactionListResponse = error("unused")
        override suspend fun withdraw(request: WithdrawTransferRequest): WithdrawTransferResponse =
            error("unused")
    }
}
