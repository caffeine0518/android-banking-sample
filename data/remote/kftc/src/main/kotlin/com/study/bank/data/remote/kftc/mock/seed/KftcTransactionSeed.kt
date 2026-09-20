package com.study.bank.data.remote.kftc.mock.seed

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 페이지네이션 시연용 거래내역 시드 — 부팅 시 `mock_transactions`에 적재된다.
 *
 * 월급통장(PAYROLL_KRW)에만 [HISTORY_COUNT]건을 생성한다 — 1천 건이 넘어 "무한 스크롤로 페이지 단위
 * 로딩"을 실제로 검증할 수 있다.
 *
 * [rows]는 오래된 순으로 반환한다 — 그 순서로 삽입하면 SQLite가 부여하는 seq가 시간순과 일치하고,
 * 이후 세션 이체는 자동으로 더 큰 seq를 받는다.
 */
internal object KftcTransactionSeed {

    const val HISTORY_COUNT = 1_200

    // 1440(하루 분)과 서로소라 1,200건의 시:분이 모두 다르다 — 거래 시각이 정각에 몰리지 않는다.
    private const val MINUTES_BETWEEN = 137L

    // 시드를 만들 무렵으로 잡은 임의 고정값. 여기서 과거로 114일치가 깔려 클라 조회 기간(2026년) 안에 든다.
    private val BASE_DATETIME: LocalDateTime = LocalDateTime.of(2026, 6, 25, 18, 0, 0)
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    private val DEPOSIT_LABELS = listOf("급여", "이자입금", "계좌이체 입금", "ATM 입금")
    private val WITHDRAW_LABELS = listOf("카드대금", "공과금", "통신비", "ATM 출금", "보험료")

    fun rows(accounts: List<SeedAccount>): List<TransactionRecord> {
        val payroll = accounts.firstOrNull { it.fintechUseNum == KftcSeedAccountIds.PAYROLL_KRW }
            ?: return emptyList()
        return historyFor(payroll).asReversed()
    }

    /** 최신 → 과거 순으로 만든다. 호출 측이 뒤집어 오래된 순으로 적재한다. */
    private fun historyFor(account: SeedAccount): List<TransactionRecord> {
        val seedBalance = BigDecimal(account.balanceAmt)
        val entries = (0 until HISTORY_COUNT).map { entryAt(it, seedBalance.scale()) }
        val balances = balancesAfter(seedBalance, entries)
        return entries.mapIndexed { index, entry -> entry.toRecord(account, balances[index]) }
    }

    /**
     * 각 거래 직후 잔액을 최신 → 과거 순으로 계산한다.
     *
     * 시드 계좌의 잔액은 '지금' 잔액이다. 그래서 최신 거래 직후 잔액이 곧 시드 잔액이고, 한 칸 과거로
     * 갈 때마다 그 거래를 취소한다 — 입금이었으면 빼고, 출금이었으면 더한다. 월급통장(시드 잔액
     * 2,847,320원)이면 이렇게 깔린다.
     *
     *     0번(최신)  급여 +1,000 입금    → 직후 2,847,320   (= 시드 잔액)
     *     1번        카드대금 -2,000 출금 → 직후 2,846,320   (0번 입금을 취소)
     *     2번        이자입금 +3,000 입금 → 직후 2,848,320   (1번 출금을 취소)
     *
     * 오래된 순으로 앞에서부터 더해 나가면 시작 잔액을 모르므로 최신 건이 시드 잔액과 어긋난다.
     */
    private fun balancesAfter(seedBalance: BigDecimal, entries: List<Entry>): List<BigDecimal> =
        entries.runningFold(seedBalance) { balance, entry ->
            if (entry.deposit) balance - entry.amount else balance + entry.amount
        }

    /**
     * index 하나로 거래 한 건을 정한다 — 짝수는 입금, 홀수는 출금. 금액은 1,000원부터 1,000원씩 올라가
     * 50,000원에서 되돌아오고, 시각은 [MINUTES_BETWEEN]분씩 과거로, 적요는 같은 방향의 라벨을 돌려 쓴다.
     *
     * now()나 난수를 쓰지 않아 몇 번을 다시 실행해도 0번은 항상 "급여 1,000원 입금"이다 — 테스트가
     * 시드 값을 문자열로 고정해 둘 수 있는 이유다.
     */
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

    /** 거래 한 건의 내용. 잔액은 생성 시점에 모르므로 [toRecord]가 받는다. */
    private data class Entry(
        val deposit: Boolean,
        val amount: BigDecimal,
        val at: LocalDateTime,
        val label: String,
    )
}
