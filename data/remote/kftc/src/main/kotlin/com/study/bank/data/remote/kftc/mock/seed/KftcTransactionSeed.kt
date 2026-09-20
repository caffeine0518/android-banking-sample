package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.TransactionRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 페이지네이션 시연용 거래내역 시드 — 부팅 시 `mock_transactions`에 적재된다.
 *
 * 월급통장(PAYROLL_KRW)에만 [HISTORY_COUNT]건을 생성한다 — 1천 건이 넘어 "무한 스크롤로 페이지 단위
 * 로딩"을 실제로 검증할 수 있다. 시각·금액·적요는 인덱스로만 정해 clock·난수에 의존하지 않는다(테스트 결정성).
 *
 * [rows]는 오래된 순으로 반환한다 — 그 순서로 삽입하면 SQLite가 부여하는 seq가 시간순과 일치하고,
 * 이후 세션 이체는 자동으로 더 큰 seq를 받는다.
 */
internal object KftcTransactionSeed {

    const val HISTORY_COUNT = 1_200

    fun rows(accounts: List<SeedAccount>): List<TransactionRecord> {
        val payroll = accounts.firstOrNull { it.fintechUseNum == KftcSeedAccountIds.PAYROLL_KRW }
            ?: return emptyList()
        return historyFor(payroll).asReversed()
    }

    /**
     * 최신 → 과거 순으로 생성한다. 최신 거래의 after_balance가 시드 잔액과 일치해야 해서 현재 잔액에서
     * 거꾸로 계산하기 때문이다. 뒤집어 적재하는 것은 호출 측 몫.
     */
    private fun historyFor(account: SeedAccount): List<TransactionRecord> {
        val scale = BigDecimal(account.balanceAmt).scale()
        var running = BigDecimal(account.balanceAmt)
        return (0 until HISTORY_COUNT).map { index ->
            val deposit = index % 2 == 0
            val amount = BigDecimal(1_000L + (index % 50) * 1_000L).setScale(scale, RoundingMode.HALF_UP)
            val afterBalance = running.setScale(scale, RoundingMode.HALF_UP)
            // 과거로 한 칸 — 입금이면 그 전엔 적었고(빼기), 출금이면 그 전엔 많았다(더하기).
            running = if (deposit) running.subtract(amount) else running.add(amount)
            val at = BASE_DATETIME.minusMinutes(index * MINUTES_BETWEEN)
            val label = labelFor(deposit, index)
            TransactionRecord(
                fintechUseNum = account.fintechUseNum,
                tranDate = at.format(DATE_FORMATTER),
                tranTime = at.format(TIME_FORMATTER),
                direction = if (deposit) TransactionDirection.DEPOSIT else TransactionDirection.WITHDRAWAL,
                printContent = label,
                tranAmt = amount.toPlainString(),
                afterBalanceAmt = afterBalance.toPlainString(),
                counterpartyName = label,
                seeded = true,
            )
        }
    }

    private fun labelFor(deposit: Boolean, index: Int): String {
        val pool = if (deposit) DEPOSIT_LABELS else WITHDRAW_LABELS
        return pool[(index / 2) % pool.size]
    }

    private const val MINUTES_BETWEEN = 137L
    // 시드 히스토리의 기준 시각(고정). 137분 간격 × 1,200건 ≈ 114일치라 from_date 범위(연초~연말) 안에 든다.
    private val BASE_DATETIME: LocalDateTime = LocalDateTime.of(2026, 6, 25, 18, 0, 0)
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    private val DEPOSIT_LABELS = listOf("급여", "이자입금", "계좌이체 입금", "ATM 입금")
    private val WITHDRAW_LABELS = listOf("카드대금", "공과금", "통신비", "ATM 출금", "보험료")
}
