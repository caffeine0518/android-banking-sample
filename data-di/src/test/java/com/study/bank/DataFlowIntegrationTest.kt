package com.study.bank

import com.study.bank.data.remote.kftc.api.KFTC_TRANSACTION_PAGE_SIZE
import com.study.bank.data.remote.kftc.mock.seed.KftcSeedAccountIds
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transaction.TransactionType
import com.study.bank.domain.model.transfer.RecipientValidation
import com.study.bank.domain.model.transfer.TransferOutcome
import com.study.bank.domain.model.transfer.TransferRequest
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.RecipientRepository
import com.study.bank.domain.repository.TransactionRepository
import com.study.bank.domain.repository.TransferRepository
import com.study.bank.domain.usecase.transfer.ExecuteTransferUseCase
import com.study.bank.domain.usecase.transfer.ValidateRecipientUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 앱은 mock KFTC + 인메모리 Room으로 실행되므로, 주입받은 Hilt 그래프가 곧 실 런타임 스택이다. */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [34])
class DataFlowIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var accountRepository: AccountRepository
    @Inject lateinit var transactionRepository: TransactionRepository
    @Inject lateinit var recipientRepository: RecipientRepository
    @Inject lateinit var transferRepository: TransferRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `계좌 상세 진입 — refresh 후 계좌가 시드 잔액으로 뜨고 월급통장 시드 거래내역이 적재된다`() = runBlocking {
        accountRepository.refresh()
        transactionRepository.refresh(SALARY)

        val account = requireNotNull(accountRepository.observeAccount(SALARY).first())
        assertEquals(Currency.KRW, account.balance.currency)
        assertEquals(0, account.balance.amount.compareTo(BigDecimal("2847320")))

        // 레거시 refresh는 첫 페이지(서버 페이지=PAGE_SIZE건)를 적재한다. 시드 1,200건이라 가득 찬 한 페이지.
        val txns = transactionRepository.observeTransactions(SALARY).first()
        assertEquals(PAGE_SIZE, txns.size)
        assertTrue(txns.all { it.accountId == SALARY })
    }

    @Test
    fun `룩업 → 송금 → 양쪽 잔액과 거래내역이 SSOT에 반영된다`() = runBlocking {
        accountRepository.refresh()

        val validation = ValidateRecipientUseCase(recipientRepository)(
            fromAccountId = SALARY,
            toAccountNumber = SAFEBOX_NUMBER,
            toBankCode = BankCode.TOSS,
        )
        assertEquals(RecipientValidation.Valid(SAFEBOX, "홍길동"), validation)

        val outcome = ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                senderName = "홍길동",
                toAccountNumber = SAFEBOX_NUMBER,
                toBankCode = BankCode.TOSS,
                recipientName = "홍길동",
                amount = Money.of(50_000L, Currency.KRW),
                memo = "세이프박스로",
                idempotencyKey = "itest-transfer-1",
            ),
        )
        assertTrue("송금 성공해야 함: $outcome", outcome is TransferOutcome.Success)

        // execute가 accountRepository.refresh로 전 계좌를 갱신한다.
        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val safebox = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())
        assertEquals(0, salary.balance.amount.compareTo(BigDecimal("2797320")))
        assertEquals(0, safebox.balance.amount.compareTo(BigDecimal("12050000")))

        // execute가 출금계좌 내역의 첫 페이지를 다시 적재하므로 맨 앞이 이번 송금이다.
        val salaryTxns = transactionRepository.observeTransactions(SALARY).first()
        assertEquals(PAGE_SIZE, salaryTxns.size)
        assertEquals(TransactionType.TRANSFER_OUT, salaryTxns.first().type)
        assertEquals(0, salaryTxns.first().amount.amount.compareTo(BigDecimal("50000")))

        // 수취계좌 내역은 그 화면 진입 시 refresh → TRANSFER_IN.
        transactionRepository.refresh(SAFEBOX)
        val safeboxTxns = transactionRepository.observeTransactions(SAFEBOX).first()
        assertEquals(1, safeboxTxns.size)
        assertEquals(TransactionType.TRANSFER_IN, safeboxTxns.first().type)
        assertEquals(0, safeboxTxns.first().amount.amount.compareTo(BigDecimal("50000")))
    }

    @Test
    fun `내 계좌로 송금 — 마스킹된 계좌번호로 보내도 수취계좌가 입금된다(앱 실제 플로우)`() = runBlocking {
        accountRepository.refresh()

        // 앱 실제 플로우: 수취계좌(내 세이프박스)를 레포에서 로드해 그 number로 송금한다.
        // list_finuse는 마스킹 번호만 주므로 앱이 가진 수취 계좌번호는 마스킹돼 있다(전체번호 모름).
        val recipient = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())
        assertTrue(
            "수취 계좌번호는 마스킹돼 있어야 함: ${recipient.number.value}",
            recipient.number.value.contains("*"),
        )

        val salaryBefore = requireNotNull(accountRepository.observeAccount(SALARY).first())
            .balance.amount
        val safeboxBefore = recipient.balance.amount

        val outcome = ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                senderName = "홍길동",
                toAccountNumber = recipient.number, // 마스킹 번호 (앱이 실제로 보내는 값)
                toBankCode = recipient.bankCode,
                recipientName = recipient.holderName,
                amount = Money.of(30_000L, Currency.KRW),
                memo = null,
                idempotencyKey = "itest-internal-masked-1",
            ),
        )
        assertTrue("송금 성공해야 함: $outcome", outcome is TransferOutcome.Success)

        // 출금계좌 차감 + 수취계좌 입금(복식부기) 둘 다 반영돼야 한다 — 회귀: 예전엔 수취계좌가 그대로였음.
        val salaryAfter = requireNotNull(accountRepository.observeAccount(SALARY).first())
            .balance.amount
        val safeboxAfter = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())
            .balance.amount
        assertEquals(0, salaryAfter.compareTo(salaryBefore - BigDecimal("30000")))
        assertEquals(0, safeboxAfter.compareTo(safeboxBefore + BigDecimal("30000")))
    }

    @Test
    fun `송금 거래내역의 상대방은 계좌번호가 아니라 명의로 표기된다`() = runBlocking {
        accountRepository.refresh()
        val source = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val recipient = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())

        // 메모 없이 송금 → 통장 인자내용(상대방 표기)이 명의로 채워져야 한다.
        val outcome = ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                senderName = source.holderName,
                toAccountNumber = recipient.number,
                toBankCode = recipient.bankCode,
                recipientName = recipient.holderName,
                amount = Money.of(20_000L, Currency.KRW),
                memo = null,
                idempotencyKey = "itest-counterparty-1",
            ),
        )
        assertTrue("송금 성공해야 함: $outcome", outcome is TransferOutcome.Success)

        transactionRepository.refresh(SALARY)
        transactionRepository.refresh(SAFEBOX)

        // 출금계좌 내역의 상대방 = 수취 명의 (예전엔 마스킹 계좌번호가 찍혔다).
        val outgoing = transactionRepository.observeTransactions(SALARY).first().first()
        assertEquals(TransactionType.TRANSFER_OUT, outgoing.type)
        assertEquals(recipient.holderName, outgoing.counterparty?.name)

        // 수취계좌 내역의 상대방 = 출금 명의 (예전엔 비어 있었다).
        val incoming = transactionRepository.observeTransactions(SAFEBOX).first().first()
        assertEquals(TransactionType.TRANSFER_IN, incoming.type)
        assertEquals(source.holderName, incoming.counterparty?.name)
    }

    @Test
    fun `다른 통화 내 계좌로 송금하면 CurrencyMismatch로 거절된다`() = runBlocking {
        accountRepository.refresh()
        val source = requireNotNull(accountRepository.observeAccount(SALARY).first())     // KRW
        val recipient = requireNotNull(accountRepository.observeAccount(FX_USD).first())  // USD

        val outcome = ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                senderName = source.holderName,
                toAccountNumber = recipient.number,
                toBankCode = recipient.bankCode,
                recipientName = recipient.holderName,
                amount = Money.of(10_000L, source.balance.currency),
                memo = null,
                idempotencyKey = "itest-currency-mismatch-1",
            ),
        )

        assertEquals(TransferOutcome.Failure.CurrencyMismatch, outcome)

        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val usd = requireNotNull(accountRepository.observeAccount(FX_USD).first())
        assertEquals(0, salary.balance.amount.compareTo(BigDecimal("2847320")))
        assertEquals(0, usd.balance.amount.compareTo(BigDecimal("3245.80")))
    }

    @Test
    fun `같은 멱등성 키로 재시도하면 한 번만 차감되고 같은 거래로 응답한다`() = runBlocking {
        accountRepository.refresh()

        // 응답을 못 받아 결과 화면이 같은 키로 다시 송금하는 상황(재시도 버튼 / 프로세스 death 복원).
        val request = TransferRequest(
            fromAccountId = SALARY,
            senderName = "홍길동",
            toAccountNumber = SAFEBOX_NUMBER,
            toBankCode = BankCode.TOSS,
            recipientName = "홍길동",
            amount = Money.of(50_000L, Currency.KRW),
            memo = null,
            idempotencyKey = "itest-idempotent-retry-1",
        )
        val first = ExecuteTransferUseCase(transferRepository)(request)
        val second = ExecuteTransferUseCase(transferRepository)(request)

        assertTrue("1차 송금 성공해야 함: $first", first is TransferOutcome.Success)
        assertTrue("재시도도 성공 응답이어야 함: $second", second is TransferOutcome.Success)
        first as TransferOutcome.Success
        second as TransferOutcome.Success

        // 멱등성 키가 bank_tran_id로 서버까지 전달돼 중복으로 판정된다 → 원장은 1건, 차감도 1회.
        assertEquals(first.result.transactionId, second.result.transactionId)
        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val safebox = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())
        assertEquals(0, salary.balance.amount.compareTo(BigDecimal("2797320")))
        assertEquals(0, safebox.balance.amount.compareTo(BigDecimal("12050000")))

        transactionRepository.refresh(SAFEBOX)
        assertEquals(1, transactionRepository.observeTransactions(SAFEBOX).first().size)
    }

    @Test
    fun `bank_tran_id 가 앞 송금과 충돌한 송금도 체결된다`() = runBlocking {
        accountRepository.refresh()
        transferToSafebox(amount = 50_000L, idempotencyKey = COLLIDING_KEY_A).requireSuccess()

        transferToSafebox(amount = 30_000L, idempotencyKey = COLLIDING_KEY_B).requireSuccess()

        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val seedMinusBothTransfers = BigDecimal(2_847_320 - 50_000 - 30_000)
        assertEquals(0, salary.balance.amount.compareTo(seedMinusBothTransfers))
    }

    @Test
    fun `bank_tran_id 가 충돌한 송금을 재시도하면 같은 거래로 응답하고 다시 차감하지 않는다`() = runBlocking {
        accountRepository.refresh()
        transferToSafebox(amount = 50_000L, idempotencyKey = COLLIDING_KEY_A).requireSuccess()
        val collided = transferToSafebox(amount = 30_000L, idempotencyKey = COLLIDING_KEY_B).requireSuccess()

        val retried = transferToSafebox(amount = 30_000L, idempotencyKey = COLLIDING_KEY_B).requireSuccess()

        assertEquals(collided.result.transactionId, retried.result.transactionId)
        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        assertEquals(0, salary.balance.amount.compareTo(BigDecimal("2767320")))
    }

    @Test
    fun `bank_tran_id 가 앞 송금과 충돌한 같은 금액의 송금도 별도 거래로 체결된다`() = runBlocking {
        accountRepository.refresh()
        val first = transferToSafebox(amount = 50_000L, idempotencyKey = COLLIDING_KEY_A).requireSuccess()

        val second = transferToSafebox(amount = 50_000L, idempotencyKey = COLLIDING_KEY_B).requireSuccess()

        assertNotEquals(first.result.transactionId, second.result.transactionId)
        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val seedMinusBothTransfers = BigDecimal(2_847_320 - 50_000 - 50_000)
        assertEquals(0, salary.balance.amount.compareTo(seedMinusBothTransfers))
    }

    private suspend fun transferToSafebox(amount: Long, idempotencyKey: String): TransferOutcome =
        ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                senderName = "홍길동",
                toAccountNumber = SAFEBOX_NUMBER,
                toBankCode = BankCode.TOSS,
                recipientName = "홍길동",
                amount = Money.of(amount, Currency.KRW),
                memo = null,
                idempotencyKey = idempotencyKey,
            ),
        )

    private fun TransferOutcome.requireSuccess(): TransferOutcome.Success =
        this as? TransferOutcome.Success ?: throw AssertionError("송금 성공해야 함: $this")

    private companion object {
        val SALARY = AccountId(KftcSeedAccountIds.PAYROLL_KRW)
        val SAFEBOX = AccountId(KftcSeedAccountIds.SAFEBOX_KRW)
        val FX_USD = AccountId(KftcSeedAccountIds.FX_USD)
        val SAFEBOX_NUMBER = AccountNumber("1000-55-1114443")

        // String.hashCode 가 같은 두 키. 해시로 bank_tran_id 를 만들면 같은 번호가 된다.
        const val COLLIDING_KEY_A = "itest-collision-Aa"
        const val COLLIDING_KEY_B = "itest-collision-BB"

        // 서버 페이지 크기 단일 소유처. 레거시 refresh는 첫 페이지 한 장을 적재한다.
        const val PAGE_SIZE = KFTC_TRANSACTION_PAGE_SIZE
    }
}
