package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object KftcTransactionSeed {

    // 무한 스크롤을 검증할 수 있는 건수.
    const val HISTORY_COUNT = 1_200

    // 1440(하루의 분 수)과 서로소라 거래 시각이 겹치지 않는다.
    private const val MINUTES_BETWEEN = 137L

    // 과거로 114일치가 생성돼 클라이언트 조회 기간(2026년)에 포함된다.
    private val BASE_DATETIME: LocalDateTime = LocalDateTime.of(2026, 6, 25, 18, 0, 0)
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    private val DEPOSIT_LABELS = listOf("급여", "이자입금", "계좌이체 입금", "ATM 입금")
    private val WITHDRAW_LABELS = listOf("카드대금", "공과금", "통신비", "ATM 출금", "보험료")

    fun rows(accounts: List<SeedAccount>): List<TransactionRecord> {
        val payroll = accounts.firstOrNull { it.fintechUseNum == KftcSeedAccountIds.PAYROLL_KRW }
            ?: return emptyList()
        // 오래된 순으로 삽입해야 SQLite가 부여하는 seq가 시간순과 일치한다.
        return historyFor(payroll).asReversed()
    }

    private fun historyFor(account: SeedAccount): List<TransactionRecord> {
        val seedBalance = BigDecimal(account.balanceAmt)
        val entries = (0 until HISTORY_COUNT).map { entryAt(it, seedBalance.scale()) }
        val balances = balancesAfter(seedBalance, entries)
        return entries.mapIndexed { index, entry -> entry.toRecord(account, balances[index]) }
    }

    /** 시드 잔액이 현재 잔액이므로 최신 거래부터 과거로 거슬러 계산한다. */
    private fun balancesAfter(seedBalance: BigDecimal, entries: List<Entry>): List<BigDecimal> =
        entries.runningFold(seedBalance) { balance, entry ->
            if (entry.deposit) balance - entry.amount else balance + entry.amount
        }

    /** now()·난수를 쓰지 않아 테스트가 시드 값을 고정할 수 있다. 0번은 항상 "급여 1,000원 입금"이다. */
    private fun entryAt(index: Int, scale: Int): Entry {
        val deposit = index % 2 == 0
        val labels = if (deposit) DEPOSIT_LABELS else WITHDRAW_LABELS
        return Entry(
            deposit = deposit,
            amount = BigDecimal(1_000L + (index % 50) * 1_000L).setScale(scale, RoundingMode.HALF_UP),
            at = BASE_DATETIME.minusMinutes(index * MINUTES_BETWEEN),
            label = labels[(index / 2) % labels.size],
        )
    }

    private fun Entry.toRecord(account: SeedAccount, balanceAfter: BigDecimal) = TransactionRecord(
        fintechUseNum = account.fintechUseNum,
        tranDate = at.format(DATE_FORMATTER),
        tranTime = at.format(TIME_FORMATTER),
        direction = if (deposit) TransactionDirection.DEPOSIT else TransactionDirection.WITHDRAWAL,
        printContent = label,
        tranAmt = amount.toPlainString(),
        afterBalanceAmt = balanceAfter.toPlainString(),
        counterpartyName = label,
        seeded = true,
    )

    private data class Entry(
        val deposit: Boolean,
        val amount: BigDecimal,
        val at: LocalDateTime,
        val label: String,
    )
}
