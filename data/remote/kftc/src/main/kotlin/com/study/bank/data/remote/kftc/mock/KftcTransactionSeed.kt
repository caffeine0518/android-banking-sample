package com.study.bank.data.remote.kftc.mock

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * 페이지네이션 시연용 거래내역 시드.
 *
 * 월급통장(PAYROLL_KRW)에 [HISTORY_COUNT]건의 과거 거래를 결정적으로 생성한다 — 1천 건이 넘어
 * "무한 스크롤로 페이지 단위 로딩"을 실제로 검증할 수 있다. 다른 계좌는 시드 히스토리가 없다(이체로만 쌓인다).
 *
 * 잔액은 가장 최신 거래의 after_balance가 시드 잔액과 일치하도록 **현재 잔액에서 과거로 거꾸로** 계산해
 * (입금은 빼고 출금은 더해) 명세서를 위→아래로 읽어도 잔액 흐름이 모순 없게 만든다. 시각/금액/적요는
 * 인덱스 기반으로만 정해 clock·난수에 의존하지 않는다(테스트 결정성).
 *
 * 결과는 결정적·불변이라 [cache]로 **프로세스당 1회만** 생성한다 — KftcBankState를 여러 번 만드는
 * 테스트가 매번 1천여 건을 재생성하지 않게 한다(컴파일 타임 상수화는 Kotlin이 List 리터럴을 지원하지 않아 불가).
 */
internal object KftcTransactionSeed {

    const val HISTORY_COUNT = 1_200

    // key = 월급통장 SeedAccount(데이터클래스 equals). 표준 시드에선 단 한 번만 채워진다.
    private val cache = ConcurrentHashMap<SeedAccount, List<TransactionRecord>>()

    fun seededHistory(accounts: List<SeedAccount>): Map<String, List<TransactionRecord>> {
        val payroll = accounts.firstOrNull { it.fintechUseNum == KftcSeedAccountIds.PAYROLL_KRW }
            ?: return emptyMap()
        return mapOf(payroll.fintechUseNum to cache.computeIfAbsent(payroll, ::historyFor))
    }

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
                // index 0(최신) → 가장 큰 seq. 시간순(BASE에서 과거로)과 seq 내림차순이 일치한다.
                seq = (HISTORY_COUNT - index).toLong(),
                tranDate = at.format(DATE_FORMATTER),
                tranTime = at.format(TIME_FORMATTER),
                direction = if (deposit) TransactionDirection.DEPOSIT else TransactionDirection.WITHDRAWAL,
                printContent = label,
                tranAmt = amount.toPlainString(),
                afterBalanceAmt = afterBalance.toPlainString(),
                counterpartyName = label,
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
