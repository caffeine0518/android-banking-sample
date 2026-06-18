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

    init {
        reset()
    }

    /** 잔액·원장을 시드 초깃값으로 되돌린다. */
    fun reset() = synchronized(lock) {
        balances.clear()
        scales.clear()
        ledger.clear()
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

    /** 최신 거래가 앞에 오는 계좌별 원장(KFTC sort_order=D). */
    fun transactions(fintechUseNum: String): List<TransactionRecord> = synchronized(lock) {
        ledger[fintechUseNum].orEmpty().toList()
    }

    fun withdraw(command: WithdrawCommand): WithdrawResult = synchronized(lock) {
        val source = seed.firstOrNull { it.fintechUseNum == command.fintechUseNum }
            ?: return WithdrawResult.UnknownSender(command.fintechUseNum)

        val amount = command.tranAmt.toBigDecimalOrNull()
        if (amount == null || amount.signum() <= 0) {
            return WithdrawResult.InvalidAmount(command.tranAmt)
        }

        val sourceBalance = balances.getValue(source.fintechUseNum)
        val sourceScale = scaleOf(source.fintechUseNum)
        if (sourceBalance < amount) {
            return WithdrawResult.InsufficientFunds(
                balance = formatBalance(source.fintechUseNum),
                attempted = format(amount, sourceScale),
            )
        }

        // 수취계좌가 시드에 있으면(내부 이체) 복식부기로 입금까지. 외부면 차감만.
        val recipient = seed.firstOrNull {
            it.accountNum == command.recvAccountNum && it.bankCodeStd == command.recvBankCode
        }
        if (recipient != null && recipient.currencyCode != source.currencyCode) {
            return WithdrawResult.CurrencyMismatch(source.currencyCode, recipient.currencyCode)
        }

        val now = clock()
        val tranDate = now.format(DATE_FORMATTER)
        val tranTime = now.format(TIME_FORMATTER)
        val amountText = format(amount, sourceScale)

        val newSource = sourceBalance - amount
        balances[source.fintechUseNum] = newSource
        prepend(
            source.fintechUseNum,
            TransactionRecord(
                tranDate = tranDate,
                tranTime = tranTime,
                direction = TransactionDirection.WITHDRAWAL,
                printContent = command.wdPrintContent ?: command.recvName,
                tranAmt = amountText,
                afterBalanceAmt = format(newSource, sourceScale),
                counterpartyName = command.recvName,
            ),
        )

        if (recipient != null) {
            val recipientScale = scaleOf(recipient.fintechUseNum)
            val newRecipient = balances.getValue(recipient.fintechUseNum) + amount
            balances[recipient.fintechUseNum] = newRecipient
            prepend(
                recipient.fintechUseNum,
                TransactionRecord(
                    tranDate = tranDate,
                    tranTime = tranTime,
                    direction = TransactionDirection.DEPOSIT,
                    printContent = command.dpsPrintContent ?: command.reqName,
                    tranAmt = format(amount, recipientScale),
                    afterBalanceAmt = format(newRecipient, recipientScale),
                    counterpartyName = command.reqName,
                ),
            )
        }

        WithdrawResult.Success(
            fintechUseNum = source.fintechUseNum,
            bankCodeStd = source.bankCodeStd,
            accountNumMasked = source.accountNumMasked,
            accountHolderName = source.accountHolderName,
            tranAmt = amountText,
            afterBalanceAmt = format(newSource, sourceScale),
        )
    }

    private fun prepend(fintechUseNum: String, record: TransactionRecord) {
        ledger.getValue(fintechUseNum).add(0, record)
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
