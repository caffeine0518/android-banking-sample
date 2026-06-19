package com.study.bank

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 데이터 레이어 E2E(L3) — UI 없이 **앱의 실제 Hilt 그래프**를 Robolectric(JVM)에서 부팅해
 * KFTC mock 서버 + Room SSOT + 실제 레포/유스케이스가 끝까지 흐르는지 검증한다.
 *
 * 수동 와이어가 아니라 @HiltAndroidTest 주입이므로 LocalModule/RepositoryModule/NetworkModule의 실제 DI
 * 배선까지 런타임 검증된다(그래프 성립 여부는 :app:kspDebugKotlin이 컴파일타임에 담당). 앱이 설계상 mock KFTC +
 * 인메모리 Room으로 돌기 때문에, 주입받은 스택이 곧 실 런타임 스택이다. HiltAndroidRule이 테스트마다 컴포넌트를
 * 새로 구성 → KftcMockServer 시드/Room이 테스트별로 초기화된다.
 */
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
    fun `계좌 상세 진입 — refresh 후 계좌가 시드 잔액으로 뜨고 거래내역은 비어 있다`() = runBlocking {
        accountRepository.refresh()
        transactionRepository.refresh(SALARY)

        val account = requireNotNull(accountRepository.observeAccount(SALARY).first())
        assertEquals(Currency.KRW, account.balance.currency)
        assertEquals(0, account.balance.amount.compareTo(BigDecimal("2847320")))

        assertTrue(transactionRepository.observeTransactions(SALARY).first().isEmpty())
    }

    @Test
    fun `룩업 → 송금 → 양쪽 잔액과 거래내역이 SSOT에 반영된다`() = runBlocking {
        accountRepository.refresh()

        // 룩업(계좌실명조회) — 본인의 다른 계좌(세이프박스)로 송금.
        val validation = ValidateRecipientUseCase(recipientRepository)(
            fromAccountId = SALARY,
            toAccountNumber = SAFEBOX_NUMBER,
            toBankCode = BankCode.TOSS,
        )
        assertEquals(RecipientValidation.Valid("홍길동"), validation)

        // 송금(출금이체) 50,000원.
        val outcome = ExecuteTransferUseCase(transferRepository)(
            TransferRequest(
                fromAccountId = SALARY,
                toAccountNumber = SAFEBOX_NUMBER,
                toBankCode = BankCode.TOSS,
                amount = Money.of(50_000L, Currency.KRW),
                memo = "세이프박스로",
                idempotencyKey = "itest-transfer-1",
            ),
        )
        assertTrue("송금 성공해야 함: $outcome", outcome is TransferOutcome.Success)

        // 잔액: 출금계좌 차감 + 수취계좌 입금(복식부기) — execute의 accountRepository.refresh가 전 계좌 갱신.
        val salary = requireNotNull(accountRepository.observeAccount(SALARY).first())
        val safebox = requireNotNull(accountRepository.observeAccount(SAFEBOX).first())
        assertEquals(0, salary.balance.amount.compareTo(BigDecimal("2797320")))
        assertEquals(0, safebox.balance.amount.compareTo(BigDecimal("12050000")))

        // 거래내역: 출금계좌엔 TRANSFER_OUT (execute가 출금계좌 내역 refresh).
        val salaryTxns = transactionRepository.observeTransactions(SALARY).first()
        assertEquals(1, salaryTxns.size)
        assertEquals(TransactionType.TRANSFER_OUT, salaryTxns.first().type)
        assertEquals(0, salaryTxns.first().amount.amount.compareTo(BigDecimal("50000")))

        // 수취계좌 내역은 그 화면 진입 시 refresh → TRANSFER_IN.
        transactionRepository.refresh(SAFEBOX)
        val safeboxTxns = transactionRepository.observeTransactions(SAFEBOX).first()
        assertEquals(1, safeboxTxns.size)
        assertEquals(TransactionType.TRANSFER_IN, safeboxTxns.first().type)
        assertEquals(0, safeboxTxns.first().amount.amount.compareTo(BigDecimal("50000")))
    }

    private companion object {
        val SALARY = AccountId("120220112345678901234001") // 월급통장 KRW 2,847,320
        val SAFEBOX = AccountId("120220112345678901234003") // 세이프박스 KRW 12,000,000
        val SAFEBOX_NUMBER = AccountNumber("1000-55-1114443")
    }
}
