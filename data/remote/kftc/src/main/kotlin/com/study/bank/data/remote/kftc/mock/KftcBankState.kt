package com.study.bank.data.remote.kftc.mock

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * KFTC mock의 가변 인메모리 상태(잔액 + 거래원장).
 *
 * 시드에서 초기화되고 [withdraw]로 잔액이 변하며 양쪽 계좌에 [TransactionRecord]를 남긴다.
 * 디스크 영속이 없어 프로세스 재시작(=앱 재실행)이 곧 초깃값 리셋이며, [reset]은 같은 효과를 명시적으로 낸다.
 *
 * 여러 스레드(요청 디스패치 vs 검증)가 접근하므로 모든 진입점을 [lock]으로 직렬화한다.
 * 잔액 문자열의 소수 자릿수(통화 exponent)는 시드 원본 문자열의 scale을 보존해 재포맷한다 —
 * 이 모듈은 :domain의 Currency를 모르기 때문.
 */
internal class KftcBankState(
    private val seed: List<SeedAccount>,
    private val clock: () -> LocalDateTime = LocalDateTime::now,
) {
    private val lock = Any()
    private val balances = LinkedHashMap<String, BigDecimal>()
    private val scales = HashMap<String, Int>()
    private val ledger = LinkedHashMap<String, MutableList<TransactionRecord>>()

    // 부팅 시 1회 생성되는 과거 거래(월급통장 1천여 건). 세션 이체로 변하는 [ledger]와 달리 불변이라 reset 대상이 아니다.
    private val seededHistory: Map<String, List<TransactionRecord>> =
        KftcTransactionSeed.seededHistory(seed)

    // 시드가 쓴 최대 seq. 세션 이체 seq는 이 위에서 증가해 항상 시드보다 최신(큰 seq)이 된다.
    private val maxSeededSeq: Long = seededHistory.values.flatten().maxOfOrNull { it.seq } ?: 0L

    // 세션 이체에 단조 증가 seq를 부여하는 카운터. lock 안에서만 접근하므로 별도 동기화 불필요.
    private var liveSeq: Long = maxSeededSeq

    init {
        reset()
    }

    /** 잔액·원장·seq 카운터를 시드 초깃값으로 되돌린다. */
    fun reset() = synchronized(lock) {
        balances.clear()
        scales.clear()
        ledger.clear()
        liveSeq = maxSeededSeq
        seed.forEach { account ->
            val parsed = BigDecimal(account.balanceAmt)
            balances[account.fintechUseNum] = parsed
            scales[account.fintechUseNum] = parsed.scale()
            ledger[account.fintechUseNum] = mutableListOf()
        }
    }

    /** 현재 잔액이 반영된 계좌 스냅샷. list_finuse/balance 응답이 기존 매퍼로 재사용. */
    fun accounts(): List<SeedAccount> = synchronized(lock) {
        seed.map { it.copy(balanceAmt = formatBalance(it.fintechUseNum)) }
    }

    fun account(fintechUseNum: String): SeedAccount? = synchronized(lock) {
        seed.firstOrNull { it.fintechUseNum == fintechUseNum }
            ?.copy(balanceAmt = formatBalance(fintechUseNum))
    }

    /** 이번 세션 이체로 쌓인 계좌별 원장(최신 우선). 시드 과거 거래는 빼고 [statement]가 합친다. */
    fun transactions(fintechUseNum: String): List<TransactionRecord> = synchronized(lock) {
        ledger[fintechUseNum].orEmpty().toList()
    }

    /**
     * 계좌 거래내역 전체(KFTC sort_order=D, 최신 우선) = 세션 이체 원장 + 시드 과거 거래를 **seq 내림차순으로 정렬**.
     *
     * seq는 단조 증가 고유값이라 전순서가 보장된다 — 같은 초 행이 있어도 커서가 strict `<`로 빠짐없이 seek할 수 있고
     * (오프셋/초단위 키의 누락 결함 회피), 화면측 Room `ORDER BY occurred_at DESC, id DESC`(id에 seq 내장)와도
     * 같은 순서가 된다. 연속조회 엔드포인트(transaction_list + befor_inquiry_trace_info 커서)가 이 명세서를 잘라 돌려준다.
     */
    fun statement(fintechUseNum: String): List<TransactionRecord> = synchronized(lock) {
        (ledger[fintechUseNum].orEmpty() + seededHistory[fintechUseNum].orEmpty())
            .sortedByDescending { it.seq }
    }

    fun withdraw(command: WithdrawCommand): WithdrawResult = synchronized(lock) {
        when (val plan = planWithdrawal(command)) {
            is WithdrawPlan.Reject -> plan.result
            is WithdrawPlan.Approved -> applyTransfer(plan, command)
        }
    }

    /** 부수효과 없는 검증. 통과하면 실행에 필요한 출금/수취 계좌·금액을 묶어 돌려주고, 아니면 거절 결과를 담는다. */
    private fun planWithdrawal(command: WithdrawCommand): WithdrawPlan {
        val source = seed.firstOrNull { it.fintechUseNum == command.fintechUseNum }
        if (source == null) {
            return WithdrawPlan.Reject(WithdrawResult.UnknownSender(command.fintechUseNum))
        }

        val amount = command.tranAmt.toBigDecimalOrNull()?.takeIf { it.signum() > 0 }
        if (amount == null) {
            return WithdrawPlan.Reject(WithdrawResult.InvalidAmount(command.tranAmt))
        }

        val isInsufficientFunds = balances.getValue(source.fintechUseNum) < amount
        if (isInsufficientFunds) {
            val insufficientFunds = WithdrawResult.InsufficientFunds(
                balance = formatBalance(source.fintechUseNum),
                attempted = format(amount, scaleOf(source.fintechUseNum)),
            )
            return WithdrawPlan.Reject(insufficientFunds)
        }

        val recipient = internalRecipientFor(command)
        val isCurrencyMismatch = recipient != null && recipient.currencyCode != source.currencyCode
        if (isCurrencyMismatch) {
            val currencyMismatch = WithdrawResult.CurrencyMismatch(source.currencyCode, recipient.currencyCode)
            return WithdrawPlan.Reject(currencyMismatch)
        }
        return WithdrawPlan.Approved(source, amount, recipient)
    }

    /** 검증 통과분에 복식부기를 적용한다 — 출금계좌 차감, 내부 수취면 같은 시각으로 입금까지(외부면 차감만). */
    private fun applyTransfer(plan: WithdrawPlan.Approved, command: WithdrawCommand): WithdrawResult.Success {
        val (source, amount, recipient) = plan
        val now = clock()
        val afterSource = post(
            fintechUseNum = source.fintechUseNum,
            direction = TransactionDirection.WITHDRAWAL,
            amount = amount,
            printContent = command.wdPrintContent ?: command.recvName,
            counterpartyName = command.recvName,
            at = now,
        )
        if (recipient != null) {
            post(
                fintechUseNum = recipient.fintechUseNum,
                direction = TransactionDirection.DEPOSIT,
                amount = amount,
                printContent = command.dpsPrintContent ?: command.reqName,
                counterpartyName = command.reqName,
                at = now,
            )
        }
        return successFor(source, amount, afterSource)
    }

    private fun successFor(source: SeedAccount, amount: BigDecimal, afterBalance: BigDecimal): WithdrawResult.Success {
        val scale = scaleOf(source.fintechUseNum)
        return WithdrawResult.Success(
            fintechUseNum = source.fintechUseNum,
            bankCodeStd = source.bankCodeStd,
            accountNumMasked = source.accountNumMasked,
            accountHolderName = source.accountHolderName,
            tranAmt = format(amount, scale),
            afterBalanceAmt = format(afterBalance, scale),
        )
    }

    /** [planWithdrawal]의 판정 결과 — 거절(이유 포함)이거나, 실행에 필요한 입력을 묶은 승인. */
    private sealed interface WithdrawPlan {
        data class Reject(val result: WithdrawResult) : WithdrawPlan
        data class Approved(
            val source: SeedAccount,
            val amount: BigDecimal,
            val recipient: SeedAccount?,
        ) : WithdrawPlan
    }

    /**
     * 수취계좌가 내 시드에 있으면 그 계좌(내부 이체 → 복식부기 대상), 외부면 null.
     * 앱은 list_finuse에서 마스킹 번호만 받으므로 내 계좌→내 계좌 송금 시 마스킹 번호로 온다.
     * 전체/마스킹 번호 둘 다로 매칭한다("*"가 있어 외부 전체번호 입력과 충돌하지 않음).
     */
    private fun internalRecipientFor(command: WithdrawCommand): SeedAccount? =
        seed.firstOrNull {
            (it.accountNum == command.recvAccountNum || it.accountNumMasked == command.recvAccountNum) &&
                it.bankCodeStd == command.recvBankCode
        }

    /**
     * 한 계좌에 입출 1건을 적용한다 — 잔액을 [direction]대로 갱신하고 원장 맨 앞에 레코드를 얹은 뒤,
     * 갱신된 잔액을 돌려준다. 호출자가 이미 [lock]을 쥔 단일 임계구역([withdraw]) 안에서만 부른다.
     */
    private fun post(
        fintechUseNum: String,
        direction: TransactionDirection,
        amount: BigDecimal,
        printContent: String,
        counterpartyName: String?,
        at: LocalDateTime,
    ): BigDecimal {
        val scale = scaleOf(fintechUseNum)
        val newBalance = when (direction) {
            TransactionDirection.WITHDRAWAL -> balances.getValue(fintechUseNum) - amount
            TransactionDirection.DEPOSIT -> balances.getValue(fintechUseNum) + amount
        }
        balances[fintechUseNum] = newBalance
        ledger.getValue(fintechUseNum).add(
            0,
            TransactionRecord(
                seq = ++liveSeq, // 시드 최대 seq 위에서 단조 증가 → 항상 시드보다 최신
                tranDate = at.format(DATE_FORMATTER),
                tranTime = at.format(TIME_FORMATTER),
                direction = direction,
                printContent = printContent,
                tranAmt = format(amount, scale),
                afterBalanceAmt = format(newBalance, scale),
                counterpartyName = counterpartyName,
            ),
        )
        return newBalance
    }

    private fun scaleOf(fintechUseNum: String): Int = scales.getValue(fintechUseNum)

    private fun formatBalance(fintechUseNum: String): String =
        format(balances.getValue(fintechUseNum), scaleOf(fintechUseNum))

    private fun format(value: BigDecimal, scale: Int): String =
        value.setScale(scale, RoundingMode.HALF_UP).toPlainString()

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    }
}
