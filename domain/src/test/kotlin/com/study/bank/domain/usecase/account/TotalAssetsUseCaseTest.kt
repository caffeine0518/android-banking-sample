package com.study.bank.domain.usecase.account

import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class TotalAssetsUseCaseTest {

    // ----- target 통화가 동적으로 결과를 결정 -----

    // UseCase의 target 파라미터가 실제로 결과를 바꾸는지 (KRW 하드코드 회귀 방지).
    @Test
    fun `같은 계좌 집합도 target에 따라 다른 통화의 converted 합계를 반환`() = runTest {
        val accounts = listOf(
            account("acc-1", 1_000_000, Currency.KRW),
            account("acc-2", "1000.00", Currency.USD),
        )
        val useCase = TotalAssetsUseCase(
            FakeAccountRepository(accounts),
            FakeFxRateRepository(ratesByTarget = mapOf(
                Currency.KRW to mapOf(
                    Currency.KRW to BigDecimal.ONE,
                    Currency.USD to BigDecimal("1350"),
                ),
                Currency.USD to mapOf(
                    Currency.USD to BigDecimal.ONE,
                    Currency.KRW to BigDecimal("0.00074074"),
                ),
            )),
        )

        val krwTotal = useCase(Currency.KRW).first()
        val usdTotal = useCase(Currency.USD).first()

        // KRW total: 1,000,000 + 1000*1350 = 2,350,000
        assertEquals(Money.of(2_350_000, Currency.KRW), krwTotal.converted)
        assertTrue(krwTotal.unconverted.isEmpty())
        // USD total: 1,000,000*0.00074074 + 1000 ≈ 1740.74
        assertEquals(Currency.USD, usdTotal.converted.currency)
        assertTrue(
            usdTotal.converted.amount > BigDecimal("1740") && usdTotal.converted.amount < BigDecimal("1741"),
            "USD total: ${usdTotal.converted.amount}",
        )
    }

    // 환율 1.0 곱셈 후 BigDecimal 반올림이 원본 값을 망가뜨리지 않는지.
    @Test
    fun `target과 같은 통화 계좌는 환산 없이 그대로 더해진다`() = runTest {
        val accounts = listOf(
            account("acc-1", "100.00", Currency.USD),
            account("acc-2", "250.50", Currency.USD),
        )
        val useCase = TotalAssetsUseCase(
            FakeAccountRepository(accounts),
            FakeFxRateRepository(ratesByTarget = mapOf(
                Currency.USD to mapOf(Currency.USD to BigDecimal.ONE),
            )),
        )

        val totals = useCase(Currency.USD).first()

        assertEquals(Money.of(BigDecimal("350.50"), Currency.USD), totals.converted)
        assertTrue(totals.unconverted.isEmpty())
    }

    // 빈 입력의 zero가 target에 종속 — 잘못된 통화로 0이 흘러가지 않게.
    @Test
    fun `계좌가 없으면 어떤 target이든 그 통화의 0과 빈 unconverted`() = runTest {
        val useCase = TotalAssetsUseCase(
            FakeAccountRepository(emptyList()),
            FakeFxRateRepository(ratesByTarget = mapOf(
                Currency.KRW to mapOf(Currency.KRW to BigDecimal.ONE),
                Currency.USD to mapOf(Currency.USD to BigDecimal.ONE),
                Currency.EUR to mapOf(Currency.EUR to BigDecimal.ONE),
            )),
        )

        listOf(Currency.KRW, Currency.USD, Currency.EUR).forEach { target ->
            val totals = useCase(target).first()
            assertEquals(Money.zero(target), totals.converted)
            assertTrue(totals.unconverted.isEmpty())
        }
    }

    // ----- 환산 불가 자산은 unconverted로 분리 -----

    // 환율 누락이 silent drop이 아니라 unconverted 리스트로 노출되는지 (이전엔 합산에서만 빠짐).
    @Test
    fun `환율 없는 통화 계좌는 unconverted에 원본 통화 그대로 담긴다`() = runTest {
        val accounts = listOf(
            account("acc-1", "100.00", Currency.USD),
            account("acc-2", "200.00", Currency.EUR), // EUR rate missing
            account("acc-3", "50.00", Currency.USD),
            account("acc-4", 30000, Currency.JPY), // JPY rate missing
        )
        val useCase = TotalAssetsUseCase(
            FakeAccountRepository(accounts),
            FakeFxRateRepository(ratesByTarget = mapOf(
                Currency.USD to mapOf(Currency.USD to BigDecimal.ONE),
            )),
        )

        val totals = useCase(Currency.USD).first()

        // converted: USD 계좌만 합산 (100 + 50 = 150)
        assertEquals(Money.of(BigDecimal("150.00"), Currency.USD), totals.converted)
        // unconverted: EUR/JPY 계좌가 원본 통화 그대로 두 항목
        assertEquals(2, totals.unconverted.size)
        assertTrue(totals.unconverted.any { it.currency == Currency.EUR })
        assertTrue(totals.unconverted.any { it.currency == Currency.JPY })
    }

    private fun account(id: String, amount: Long, currency: Currency) = baseAccount(id, Money.of(amount, currency))

    private fun account(id: String, amount: String, currency: Currency) = baseAccount(id, Money.of(BigDecimal(amount), currency))

    private fun baseAccount(id: String, balance: Money) = Account(
        id = AccountId(id),
        number = AccountNumber(id.replace("-", "")),
        bankCode = BankCode.TOSS,
        holderName = "홍길동",
        balance = balance,
        type = AccountType.CHECKING,
    )

    private class FakeAccountRepository(private val accounts: List<Account>) : AccountRepository {
        override fun observeAccounts(): Flow<List<Account>> = flowOf(accounts)
        override fun observeAccount(id: AccountId): Flow<Account?> =
            flowOf(accounts.firstOrNull { it.id == id })
        override suspend fun findAccount(id: AccountId): Account? =
            accounts.firstOrNull { it.id == id }
        override suspend fun refresh() = Unit
    }

    private class FakeFxRateRepository(
        private val ratesByTarget: Map<Currency, Map<Currency, BigDecimal>>,
    ) : FxRateRepository {
        override fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>> =
            flowOf(ratesByTarget[target] ?: mapOf(target to BigDecimal.ONE))
    }
}
