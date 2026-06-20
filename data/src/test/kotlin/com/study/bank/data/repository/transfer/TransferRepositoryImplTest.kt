package com.study.bank.data.repository.transfer

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * [TransferRepositoryImpl] 검증. KFTC withdraw 응답을 모사하고, 성공 시 SSOT 재동기화(refresh) 호출과
 * 결과/실패 매핑을 본다. AccountRepository/TransactionRepository는 refresh 호출만 기록하는 페이크.
 */
class TransferRepositoryImplTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-06-18T01:30:00Z"), ZoneOffset.UTC)

    @Test
    fun `성공 응답이면 Success와 잔액·완료시각을 돌려주고 SSOT를 갱신한다`() = runTest {
        val api = FakeKftcApiService { successResponse(after = "2797320", bankTranId = "M202300001U000007") }
        val accounts = FakeAccountRepository()
        val transactions = FakeTransactionRepository()
        val repo = TransferRepositoryImpl(api, accounts, transactions, fixedClock)

        val outcome = repo.execute(request())

        assertTrue(outcome is TransferOutcome.Success)
        outcome as TransferOutcome.Success
        assertEquals("M202300001U000007", outcome.result.transactionId.value)
        assertEquals(TransactionStatus.COMPLETED, outcome.result.status)
        assertEquals(Currency.KRW, outcome.result.balanceAfter.currency)
        assertEquals(0, outcome.result.balanceAfter.amount.compareTo(BigDecimal("2797320")))
        assertEquals(Instant.parse("2026-06-18T01:30:00Z"), outcome.result.completedAt)

        // SSOT 재동기화 호출 확인.
        assertEquals(1, accounts.refreshCount)
        assertEquals(listOf(AccountId("120220112345678901234001")), transactions.refreshedAccounts)
    }

    @Test
    fun `잔액부족(A0001+311)이면 InsufficientFunds이고 SSOT를 갱신하지 않는다`() = runTest {
        val api = FakeKftcApiService { failureResponse(bankRspCode = "311") }
        val accounts = FakeAccountRepository()
        val transactions = FakeTransactionRepository()
        val repo = TransferRepositoryImpl(api, accounts, transactions, fixedClock)

        val outcome = repo.execute(request())

        assertEquals(TransferOutcome.Failure.InsufficientFunds, outcome)
        assertEquals(0, accounts.refreshCount)
        assertTrue(transactions.refreshedAccounts.isEmpty())
    }

    @Test
    fun `통화 불일치(A0001+320)면 CurrencyMismatch이고 SSOT를 갱신하지 않는다`() = runTest {
        val api = FakeKftcApiService { failureResponse(bankRspCode = "320") }
        val accounts = FakeAccountRepository()
        val transactions = FakeTransactionRepository()
        val repo = TransferRepositoryImpl(api, accounts, transactions, fixedClock)

        val outcome = repo.execute(request())

        assertEquals(TransferOutcome.Failure.CurrencyMismatch, outcome)
        assertEquals(0, accounts.refreshCount)
        assertTrue(transactions.refreshedAccounts.isEmpty())
    }

    @Test
    fun `알 수 없는 업무 거절은 Unknown으로 매핑된다`() = runTest {
        val api = FakeKftcApiService { failureResponse(bankRspCode = "999") }
        val repo = TransferRepositoryImpl(api, FakeAccountRepository(), FakeTransactionRepository(), fixedClock)

        assertTrue(repo.execute(request()) is TransferOutcome.Failure.Unknown)
    }

    @Test
    fun `네트워크 예외는 Network로 매핑된다`() = runTest {
        val api = FakeKftcApiService { throw IOException("no network") }
        val repo = TransferRepositoryImpl(api, FakeAccountRepository(), FakeTransactionRepository(), fixedClock)

        assertTrue(repo.execute(request()) is TransferOutcome.Failure.Network)
    }

    @Test
    fun `그 외 예외는 Unknown으로 매핑된다`() = runTest {
        val api = FakeKftcApiService { throw RuntimeException("boom") }
        val repo = TransferRepositoryImpl(api, FakeAccountRepository(), FakeTransactionRepository(), fixedClock)

        assertTrue(repo.execute(request()) is TransferOutcome.Failure.Unknown)
    }

    @Test
    fun `요청은 출금계좌·수취계좌·금액을 KFTC 필드로 전송한다`() = runTest {
        val api = FakeKftcApiService { successResponse() }
        val repo = TransferRepositoryImpl(api, FakeAccountRepository(), FakeTransactionRepository(), fixedClock)

        repo.execute(request())

        val sent = api.lastWithdraw
        assertEquals("120220112345678901234001", sent?.fintechUseNum)
        assertEquals("1000-55-1114443", sent?.recvClientAccountNum)
        assertEquals("092", sent?.recvClientBankCodeStd) // BankCode.TOSS
        assertEquals("50000", sent?.tranAmt)
        // 이름은 계좌번호가 아니라 출금/수취 명의로 전송돼야 한다(거래내역 상대방 표기).
        assertEquals("홍길동", sent?.reqClientName)
        assertEquals("김세이프", sent?.recvClientName)
    }

    private fun request() = TransferRequest(
        fromAccountId = AccountId("120220112345678901234001"),
        senderName = "홍길동",
        toAccountNumber = AccountNumber("1000-55-1114443"),
        toBankCode = BankCode.TOSS,
        recipientName = "김세이프",
        amount = Money.of(50_000, Currency.KRW),
        memo = "세이프박스로",
        idempotencyKey = "idem-1",
    )

    private fun successResponse(
        after: String = "2797320",
        bankTranId: String = "M202300001U000001",
    ) = WithdrawTransferResponse(
        apiTranId = "T0000000000000001",
        apiTranDtm = "20260618103000000",
        rspCode = "A0000",
        rspMessage = "",
        bankTranId = bankTranId,
        afterBalanceAmt = after,
    )

    private fun failureResponse(bankRspCode: String) = WithdrawTransferResponse(
        apiTranId = "T0000000000000001",
        apiTranDtm = "20260618103000000",
        rspCode = "A0001",
        rspMessage = "거절",
        bankRspCode = bankRspCode,
    )

    private class FakeAccountRepository : AccountRepository {
        var refreshCount = 0
            private set

        override fun observeAccounts(): Flow<List<Account>> = emptyFlow()
        override fun observeAccount(id: AccountId): Flow<Account?> = emptyFlow()
        override suspend fun findAccount(id: AccountId): Account? = null
        override suspend fun refresh() {
            refreshCount++
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        val refreshedAccounts = mutableListOf<AccountId>()

        override fun observeTransactions(accountId: AccountId): Flow<List<Transaction>> = emptyFlow()
        override suspend fun refresh(accountId: AccountId) {
            refreshedAccounts += accountId
        }
    }

    private class FakeKftcApiService(
        private val onWithdraw: () -> WithdrawTransferResponse,
    ) : KftcApiService {
        var lastWithdraw: WithdrawTransferRequest? = null
            private set

        override suspend fun withdraw(request: WithdrawTransferRequest): WithdrawTransferResponse {
            lastWithdraw = request
            return onWithdraw()
        }

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
        override suspend fun inquireRealName(request: RealNameInquiryRequest): RealNameInquiryResponse =
            error("unused")
    }
}
