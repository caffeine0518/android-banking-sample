package com.study.bank.data.remote.kftc.mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/**
 * [KftcBankState] 단독 검증.
 *
 * HTTP/직렬화 없이 잔액·원장 변동 로직만 본다(라우팅은 KftcMockDispatcherTest, E2E는 KftcApiServiceTest).
 * 기본 시드를 그대로 쓰되 시각은 고정 clock으로 결정적이게 만든다.
 */
class KftcBankStateTest {

    // 시드 히스토리 최신(2026-06-25 18:00)보다 뒤여야 세션 이체가 실제로도 최신 — 프로덕션(now>시드)과 같은 전제.
    private val fixedClock = { LocalDateTime.of(2026, 6, 27, 10, 30, 0) }

    private fun newState() = KftcBankState(KftcAccountSeed.accounts, fixedClock)

    @Test
    fun `초기 잔액은 시드 그대로이고 원장은 비어 있다`() {
        val state = newState()

        assertEquals("2847320", state.account(SALARY)!!.balanceAmt)
        assertEquals("3245.80", state.account(USD)!!.balanceAmt)
        assertTrue(state.transactions(SALARY).isEmpty())
    }

    @Test
    fun `외부 수취 출금이체는 출금계좌만 차감하고 TRANSFER_OUT을 기록한다`() {
        val state = newState()

        val result = state.withdraw(externalCommand(from = SALARY, amount = "50000"))

        assertTrue(result is WithdrawResult.Success)
        assertEquals("2797320", (result as WithdrawResult.Success).afterBalanceAmt)
        assertEquals("2797320", state.account(SALARY)!!.balanceAmt)

        val ledger = state.transactions(SALARY)
        assertEquals(1, ledger.size)
        assertEquals(TransactionDirection.WITHDRAWAL, ledger.first().direction)
        assertEquals("50000", ledger.first().tranAmt)
        assertEquals("2797320", ledger.first().afterBalanceAmt)
    }

    @Test
    fun `수취계좌가 시드에 있으면 복식부기로 입금까지 기록한다`() {
        val state = newState()

        state.withdraw(internalCommand(from = SALARY, toAccountNum = SAFEBOX_NUM, amount = "50000"))

        // 출금계좌 차감, 수취계좌 입금.
        assertEquals("2797320", state.account(SALARY)!!.balanceAmt)
        assertEquals("12050000", state.account(SAFEBOX)!!.balanceAmt)

        val source = state.transactions(SALARY)
        assertEquals(1, source.size)
        assertEquals(TransactionDirection.WITHDRAWAL, source.first().direction)

        val dest = state.transactions(SAFEBOX)
        assertEquals(1, dest.size)
        assertEquals(TransactionDirection.DEPOSIT, dest.first().direction)
        assertEquals("50000", dest.first().tranAmt)
        assertEquals("12050000", dest.first().afterBalanceAmt)
    }

    @Test
    fun `잔액 부족이면 실패하고 상태를 변경하지 않는다`() {
        val state = newState()

        val result = state.withdraw(externalCommand(from = SALARY, amount = "999999999"))

        assertTrue(result is WithdrawResult.InsufficientFunds)
        assertEquals("2847320", state.account(SALARY)!!.balanceAmt)
        assertTrue(state.transactions(SALARY).isEmpty())
    }

    @Test
    fun `내부 수취계좌 통화가 다르면 통화불일치로 실패하고 양쪽 상태를 보존한다`() {
        val state = newState()

        // SALARY(KRW) → USD 외화통장(092, 1000-98-7654321)
        val result = state.withdraw(internalCommand(from = SALARY, toAccountNum = USD_NUM, amount = "1000"))

        assertTrue(result is WithdrawResult.CurrencyMismatch)
        assertEquals("2847320", state.account(SALARY)!!.balanceAmt)
        assertEquals("3245.80", state.account(USD)!!.balanceAmt)
        assertTrue(state.transactions(SALARY).isEmpty())
        assertTrue(state.transactions(USD).isEmpty())
    }

    @Test
    fun `존재하지 않는 출금 fintech_use_num은 UnknownSender`() {
        val result = newState().withdraw(externalCommand(from = "999999999999999999999999", amount = "1000"))

        assertTrue(result is WithdrawResult.UnknownSender)
    }

    @Test
    fun `0이거나 음수거나 숫자가 아닌 tran_amt는 InvalidAmount`() {
        val state = newState()

        assertTrue(state.withdraw(externalCommand(from = SALARY, amount = "0")) is WithdrawResult.InvalidAmount)
        assertTrue(state.withdraw(externalCommand(from = SALARY, amount = "-100")) is WithdrawResult.InvalidAmount)
        assertTrue(state.withdraw(externalCommand(from = SALARY, amount = "abc")) is WithdrawResult.InvalidAmount)
        // 거절들은 상태를 건드리지 않는다.
        assertEquals("2847320", state.account(SALARY)!!.balanceAmt)
    }

    @Test
    fun `USD 출금이체는 소수 둘째 자리 잔액을 유지한다`() {
        val state = newState()

        val result = state.withdraw(externalCommand(from = USD, amount = "45.80"))

        assertEquals("3200.00", (result as WithdrawResult.Success).afterBalanceAmt)
        assertEquals("3200.00", state.account(USD)!!.balanceAmt)
    }

    @Test
    fun `reset은 잔액과 원장을 시드 초깃값으로 되돌린다`() {
        val state = newState()
        state.withdraw(externalCommand(from = SALARY, amount = "50000"))

        state.reset()

        assertEquals("2847320", state.account(SALARY)!!.balanceAmt)
        assertTrue(state.transactions(SALARY).isEmpty())
    }

    @Test
    fun `원장은 최신 거래가 앞에 온다`() {
        val state = newState()
        state.withdraw(externalCommand(from = SALARY, amount = "10000", recvName = "첫번째"))
        state.withdraw(externalCommand(from = SALARY, amount = "20000", recvName = "두번째"))

        val ledger = state.transactions(SALARY)

        assertEquals(2, ledger.size)
        assertEquals("두번째", ledger[0].counterpartyName)
        assertEquals("첫번째", ledger[1].counterpartyName)
        assertEquals("2817320", ledger[0].afterBalanceAmt) // 2847320 - 10000 - 20000
    }

    // --- 페이지네이션용 시드 거래내역(statement) ---

    @Test
    fun `월급통장 statement는 1천 건 이상의 시드 거래내역을 노출한다`() {
        val statement = newState().statement(SALARY)

        assertTrue("시드 거래가 1000건 이상이어야 한다: ${statement.size}", statement.size >= 1000)
        assertEquals(KftcTransactionSeed.HISTORY_COUNT, statement.size)
    }

    @Test
    fun `statement 최신 거래의 잔액은 시드 현재 잔액과 일치한다`() {
        assertEquals("2847320", newState().statement(SALARY).first().afterBalanceAmt)
    }

    @Test
    fun `시드 히스토리는 월급통장에만 있고 세션 원장과 다른 계좌는 비어 있다`() {
        val state = newState()

        assertTrue(state.statement(USD).isEmpty())
        assertTrue("세션 이체 원장은 여전히 비어 있어야 한다", state.transactions(SALARY).isEmpty())
    }

    @Test
    fun `세션 이체는 시드보다 큰 seq를 받아 statement 맨 앞에 온다`() {
        val state = newState()

        state.withdraw(externalCommand(from = SALARY, amount = "50000"))

        val statement = state.statement(SALARY)
        assertEquals(KftcTransactionSeed.HISTORY_COUNT + 1, statement.size)
        // statement는 seq(기록순) 내림차순 정렬. 세션 이체 seq > 모든 시드 seq라 맨 앞.
        assertEquals(TransactionDirection.WITHDRAWAL, statement.first().direction)
        assertEquals("2797320", statement.first().afterBalanceAmt)
    }

    @Test
    fun `각 거래는 단조 증가 seq를 받고 statement는 seq 내림차순이다`() {
        val state = newState()

        state.withdraw(externalCommand(from = SALARY, amount = "10000", recvName = "첫번째"))
        state.withdraw(externalCommand(from = SALARY, amount = "20000", recvName = "두번째"))

        val statement = state.statement(SALARY)
        // 나중에 기록된 "두번째"가 더 큰 seq → 맨 앞. seq는 전체적으로 내림차순.
        assertEquals("두번째", statement[0].counterpartyName)
        assertEquals("첫번째", statement[1].counterpartyName)
        assertTrue("seq가 엄격히 내림차순이어야", statement.zipWithNext().all { (a, b) -> a.seq > b.seq })
    }

    private fun externalCommand(from: String, amount: String, recvName: String = "외부수취인") = WithdrawCommand(
        fintechUseNum = from,
        tranAmt = amount,
        recvAccountNum = "9999-99-9999999", // 시드에 없는 계좌번호 → 외부 이체
        recvBankCode = "004",
        recvName = recvName,
        reqName = "홍길동",
        wdPrintContent = null,
        dpsPrintContent = null,
    )

    private fun internalCommand(from: String, toAccountNum: String, amount: String) = WithdrawCommand(
        fintechUseNum = from,
        tranAmt = amount,
        recvAccountNum = toAccountNum,
        recvBankCode = "092", // 토스뱅크 시드 계좌
        recvName = "홍길동",
        reqName = "홍길동",
        wdPrintContent = "보냄",
        dpsPrintContent = "받음",
    )

    private companion object {
        const val SALARY = KftcSeedAccountIds.PAYROLL_KRW
        const val USD = KftcSeedAccountIds.FX_USD
        const val SAFEBOX = KftcSeedAccountIds.SAFEBOX_KRW
        const val SAFEBOX_NUM = "1000-55-1114443"
        const val USD_NUM = "1000-98-7654321"
    }
}
