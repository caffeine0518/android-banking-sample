package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.TestMockBank
import com.study.bank.data.remote.kftc.mock.seed.KftcSeedAccountIds
import com.study.bank.data.remote.kftc.mock.seed.KftcTransactionSeed
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionDirection
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * HTTP/직렬화 없이 [KftcWithdrawalService]의 잔액·원장 변동 로직만 검증한다.
 * 기본 시드를 그대로 쓰되 시각은 고정 clock으로 결정적이게 만든다.
 * Room이 Context를 요구하므로 Robolectric에서 실행한다 — 테스트마다 새 인메모리 DB라 서로 격리된다.
 */
@RunWith(RobolectricTestRunner::class)
class KftcWithdrawalServiceTest {

    // 시드 히스토리 최신(2026-06-25 18:00)보다 뒤여야 세션 이체가 실제로도 최신 — 프로덕션(now>시드)과 같은 전제.
    private val fixedClock: Clock = Clock.fixed(
        LocalDateTime.of(2026, 6, 27, 10, 30, 0).atZone(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault(),
    )

    private fun newBank() = TestMockBank(clock = fixedClock)

    @Test
    fun `초기 잔액은 시드 그대로이고 원장은 비어 있다`() {
        val bank = newBank()

        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertEquals("3245.80", bank.accountDao.find(USD)!!.balanceAmt)
        assertTrue(bank.transactionDao.sessionLedger(SALARY).isEmpty())
    }

    @Test
    fun `외부 수취 출금이체는 출금계좌만 차감하고 TRANSFER_OUT을 기록한다`() {
        val bank = newBank()

        val result = bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000"))

        assertTrue(result is WithdrawResult.Success)
        assertEquals("2797320", (result as WithdrawResult.Success).afterBalanceAmt)
        assertEquals("2797320", bank.accountDao.find(SALARY)!!.balanceAmt)

        val ledger = bank.transactionDao.sessionLedger(SALARY)
        assertEquals(1, ledger.size)
        assertEquals(TransactionDirection.WITHDRAWAL, ledger.first().direction)
        assertEquals("50000", ledger.first().tranAmt)
        assertEquals("2797320", ledger.first().afterBalanceAmt)
    }

    @Test
    fun `수취계좌가 시드에 있으면 복식부기로 입금까지 기록한다`() {
        val bank = newBank()

        bank.withdrawalService.withdraw(internalCommand(from = SALARY, toAccountNum = SAFEBOX_NUM, amount = "50000"))

        // 출금계좌 차감, 수취계좌 입금.
        assertEquals("2797320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertEquals("12050000", bank.accountDao.find(SAFEBOX)!!.balanceAmt)

        val source = bank.transactionDao.sessionLedger(SALARY)
        assertEquals(1, source.size)
        assertEquals(TransactionDirection.WITHDRAWAL, source.first().direction)

        val dest = bank.transactionDao.sessionLedger(SAFEBOX)
        assertEquals(1, dest.size)
        assertEquals(TransactionDirection.DEPOSIT, dest.first().direction)
        assertEquals("50000", dest.first().tranAmt)
        assertEquals("12050000", dest.first().afterBalanceAmt)
    }

    @Test
    fun `출금계좌와 수취계좌가 같으면 차감과 입금이 상쇄돼 잔액이 보존된다`() {
        val bank = newBank()

        // 내 계좌로 내가 보내는 경우 — 같은 행에 출금·입금이 연달아 적용된다.
        bank.withdrawalService.withdraw(internalCommand(from = SALARY, toAccountNum = SALARY_NUM, amount = "50000"))

        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
        // 원장에는 출금·입금 두 줄이 남는다.
        val ledger = bank.transactionDao.sessionLedger(SALARY)
        assertEquals(2, ledger.size)
        assertEquals(TransactionDirection.DEPOSIT, ledger[0].direction)
        assertEquals("2847320", ledger[0].afterBalanceAmt)
        assertEquals(TransactionDirection.WITHDRAWAL, ledger[1].direction)
        assertEquals("2797320", ledger[1].afterBalanceAmt)
    }

    @Test
    fun `잔액 부족이면 실패하고 상태를 변경하지 않는다`() {
        val bank = newBank()

        val result = bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "999999999"))

        assertTrue(result is WithdrawResult.InsufficientFunds)
        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertTrue(bank.transactionDao.sessionLedger(SALARY).isEmpty())
    }

    @Test
    fun `내부 수취계좌 통화가 다르면 통화불일치로 실패하고 양쪽 상태를 보존한다`() {
        val bank = newBank()

        // SALARY(KRW) → USD 외화통장(092, 1000-98-7654321)
        val result = bank.withdrawalService.withdraw(internalCommand(from = SALARY, toAccountNum = USD_NUM, amount = "1000"))

        assertTrue(result is WithdrawResult.CurrencyMismatch)
        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertEquals("3245.80", bank.accountDao.find(USD)!!.balanceAmt)
        assertTrue(bank.transactionDao.sessionLedger(SALARY).isEmpty())
        assertTrue(bank.transactionDao.sessionLedger(USD).isEmpty())
    }

    @Test
    fun `존재하지 않는 출금 fintech_use_num은 UnknownSender`() {
        val result = newBank().withdrawalService.withdraw(externalCommand(from = "999999999999999999999999", amount = "1000"))

        assertTrue(result is WithdrawResult.UnknownSender)
    }

    @Test
    fun `0이거나 음수거나 숫자가 아닌 tran_amt는 InvalidAmount`() {
        val bank = newBank()

        assertTrue(bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "0")) is WithdrawResult.InvalidAmount)
        assertTrue(bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "-100")) is WithdrawResult.InvalidAmount)
        assertTrue(bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "abc")) is WithdrawResult.InvalidAmount)
        // 거절 건은 상태를 변경하지 않는다.
        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
    }

    @Test
    fun `USD 출금이체는 소수 둘째 자리 잔액을 유지한다`() {
        val bank = newBank()

        val result = bank.withdrawalService.withdraw(externalCommand(from = USD, amount = "45.80"))

        assertEquals("3200.00", (result as WithdrawResult.Success).afterBalanceAmt)
        assertEquals("3200.00", bank.accountDao.find(USD)!!.balanceAmt)
    }

    @Test
    fun `재적재는 잔액과 원장을 시드 초깃값으로 되돌린다`() {
        val bank = newBank()
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000", bankTranId = REPLAYED_ID))

        bank.reseed()

        assertEquals("2847320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertTrue(bank.transactionDao.sessionLedger(SALARY).isEmpty())
        // 멱등 기록도 비워져야 한다 — 남아 있으면 초기화 후 같은 번호의 송금이 중복으로 판정된다.
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000", bankTranId = REPLAYED_ID))
        assertEquals("2797320", bank.accountDao.find(SALARY)!!.balanceAmt)
    }

    // --- 멱등성(bank_tran_id 중복 판정) ---

    @Test
    fun `같은 bank_tran_id로 다시 출금하면 한 번만 차감하고 같은 응답을 돌려준다`() {
        val bank = newBank()

        // 응답이 유실돼 클라이언트가 같은 멱등성 키로 재시도하는 상황.
        val first = bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000", bankTranId = REPLAYED_ID))
        val second = bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000", bankTranId = REPLAYED_ID))

        assertEquals("2797320", bank.accountDao.find(SALARY)!!.balanceAmt)
        assertEquals(1, bank.transactionDao.sessionLedger(SALARY).size)
        // 재요청은 원장을 변경하지 않고 처음 체결한 응답을 그대로 반환한다(거래고유번호까지 동일).
        assertEquals(first, second)
        assertEquals(REPLAYED_ID, (second as WithdrawResult.Success).bankTranId)
    }

    @Test
    fun `거절된 요청은 같은 bank_tran_id로 다시 시도할 수 있다`() {
        val bank = newBank()

        // 잔액 부족으로 거절 = 원장 미변경. 같은 번호로 다시 보내면 정상 체결돼야 한다.
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "999999999", bankTranId = REPLAYED_ID))
        val retried = bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000", bankTranId = REPLAYED_ID))

        assertTrue(retried is WithdrawResult.Success)
        assertEquals("2797320", bank.accountDao.find(SALARY)!!.balanceAmt)
    }

    @Test
    fun `원장은 최신 거래가 앞에 온다`() {
        val bank = newBank()
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "10000", recvName = "첫번째"))
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "20000", recvName = "두번째"))

        val ledger = bank.transactionDao.sessionLedger(SALARY)

        assertEquals(2, ledger.size)
        assertEquals("두번째", ledger[0].counterpartyName)
        assertEquals("첫번째", ledger[1].counterpartyName)
        assertEquals("2817320", ledger[0].afterBalanceAmt) // 2847320 - 10000 - 20000
    }

    // --- 페이지네이션용 시드 거래내역(statement) ---

    @Test
    fun `월급통장 statement는 1천 건 이상의 시드 거래내역을 노출한다`() {
        val statement = newBank().transactionDao.statement(SALARY)

        assertTrue("시드 거래가 1000건 이상이어야 한다: ${statement.size}", statement.size >= 1000)
        assertEquals(KftcTransactionSeed.HISTORY_COUNT, statement.size)
    }

    @Test
    fun `statement 최신 거래의 잔액은 시드 현재 잔액과 일치한다`() {
        assertEquals("2847320", newBank().transactionDao.statement(SALARY).first().afterBalanceAmt)
    }

    @Test
    fun `시드 히스토리는 월급통장에만 있고 세션 원장과 다른 계좌는 비어 있다`() {
        val bank = newBank()

        assertTrue(bank.transactionDao.statement(USD).isEmpty())
        assertTrue("세션 이체 원장은 여전히 비어 있어야 한다", bank.transactionDao.sessionLedger(SALARY).isEmpty())
    }

    @Test
    fun `세션 이체는 시드보다 큰 seq를 받아 statement 맨 앞에 온다`() {
        val bank = newBank()

        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "50000"))

        val statement = bank.transactionDao.statement(SALARY)
        assertEquals(KftcTransactionSeed.HISTORY_COUNT + 1, statement.size)
        // statement는 seq(기록순) 내림차순 정렬. 세션 이체 seq > 모든 시드 seq라 맨 앞.
        assertEquals(TransactionDirection.WITHDRAWAL, statement.first().direction)
        assertEquals("2797320", statement.first().afterBalanceAmt)
    }

    @Test
    fun `각 거래는 단조 증가 seq를 받고 statement는 seq 내림차순이다`() {
        val bank = newBank()

        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "10000", recvName = "첫번째"))
        bank.withdrawalService.withdraw(externalCommand(from = SALARY, amount = "20000", recvName = "두번째"))

        val statement = bank.transactionDao.statement(SALARY)
        // 나중에 기록된 "두번째"가 더 큰 seq → 맨 앞. seq는 전체적으로 내림차순.
        assertEquals("두번째", statement[0].counterpartyName)
        assertEquals("첫번째", statement[1].counterpartyName)
        assertTrue("seq가 엄격히 내림차순이어야", statement.zipWithNext().all { (a, b) -> a.seq > b.seq })
    }

    // 명시하지 않으면 호출마다 새 거래고유번호를 부여한다 — 서로 다른 송금이 중복 판정에 걸리지 않게.
    private var tranSeq = 0
    private fun newTranId(): String = "M202300001U%09d".format(++tranSeq)

    private fun externalCommand(
        from: String,
        amount: String,
        recvName: String = "외부수취인",
        bankTranId: String = newTranId(),
    ) = WithdrawCommand(
        bankTranId = bankTranId,
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
        bankTranId = newTranId(),
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
        // 재시도가 같은 송금임을 나타내는 고정 거래고유번호.
        const val REPLAYED_ID = "M202300001U000000777"
        const val SALARY = KftcSeedAccountIds.PAYROLL_KRW
        const val USD = KftcSeedAccountIds.FX_USD
        const val SAFEBOX = KftcSeedAccountIds.SAFEBOX_KRW
        const val SALARY_NUM = "1000-12-3456789"
        const val SAFEBOX_NUM = "1000-55-1114443"
        const val USD_NUM = "1000-98-7654321"
    }
}
