package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.TransactionRecord
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * [KftcWithdrawalService]를 Room 원장 위에 구현한다.
 *
 * 원자성은 [transactionScope]가 보장한다 — 세 테이블을 함께 바꾸기 때문.
 * 잔액 문자열의 소수 자릿수(통화 exponent)는 저장된 문자열의 scale을 그대로 보존해 재포맷한다 —
 * 이 모듈은 :domain의 Currency를 모르기 때문.
 */
internal class KftcWithdrawalServiceImpl @Inject constructor(
    private val transactionScope: MockTransactionScopeDao,
    private val accountDao: MockAccountDao,
    private val transactionDao: MockTransactionDao,
    private val withdrawalDao: MockWithdrawalDao,
    private val clock: Clock,
) : KftcWithdrawalService {

    override fun withdraw(command: WithdrawCommand): WithdrawResult = transactionScope.inTransaction {
        withdrawalDao.find(command.bankTranId)
            ?: when (val plan = planWithdrawal(command)) {
                is WithdrawPlan.Reject -> plan.result
                is WithdrawPlan.Approved -> applyTransfer(plan, command).also(withdrawalDao::insert)
            }
    }

    /** 부수효과 없는 검증. 통과하면 실행에 필요한 출금/수취 계좌·금액을 묶어 반환하고, 아니면 거절 결과를 반환한다. */
    private fun planWithdrawal(command: WithdrawCommand): WithdrawPlan {
        val source = accountDao.find(command.fintechUseNum)
            ?: return WithdrawPlan.Reject(WithdrawResult.UnknownSender(command.fintechUseNum))

        val amount = command.tranAmt.toBigDecimalOrNull()?.takeIf { it.signum() > 0 }
            ?: return WithdrawPlan.Reject(WithdrawResult.InvalidAmount(command.tranAmt))

        val balance = BigDecimal(source.balanceAmt)
        if (balance < amount) {
            val insufficientFunds = WithdrawResult.InsufficientFunds(
                balance = source.balanceAmt,
                attempted = format(amount, balance.scale()),
            )
            return WithdrawPlan.Reject(insufficientFunds)
        }

        // 수취계좌가 내 시드에 있으면 내부 이체(복식부기 대상), 없으면(null) 외부 이체.
        val recipient = accountDao.findByAccountNum(command.recvBankCode, command.recvAccountNum)
        if (recipient != null && recipient.currencyCode != source.currencyCode) {
            return WithdrawPlan.Reject(
                WithdrawResult.CurrencyMismatch(source.currencyCode, recipient.currencyCode),
            )
        }
        return WithdrawPlan.Approved(source, amount, recipient)
    }

    /** 검증 통과분에 복식부기를 적용한다 — 출금계좌 차감, 내부 수취면 같은 시각으로 입금까지(외부면 차감만). */
    private fun applyTransfer(plan: WithdrawPlan.Approved, command: WithdrawCommand): WithdrawResult.Success {
        val (source, amount, recipient) = plan
        val now = LocalDateTime.now(clock)
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
        return WithdrawResult.Success(
            bankTranId = command.bankTranId,
            fintechUseNum = source.fintechUseNum,
            bankCodeStd = source.bankCodeStd,
            accountNumMasked = source.accountNumMasked,
            accountHolderName = source.accountHolderName,
            tranAmt = format(amount, BigDecimal(source.balanceAmt).scale()),
            afterBalanceAmt = afterSource,
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
     * 한 계좌에 입출 1건을 적용한다 — 잔액을 [direction]대로 갱신하고 원장에 한 행을 추가한 뒤,
     * 갱신된 잔액 문자열을 반환한다. [withdraw]의 트랜잭션 안에서만 호출한다.
     *
     * 잔액은 스냅샷이 아니라 **매번 테이블에서 다시 조회한다** — 내 계좌로 내가 보내면(출금=수취)
     * 입금 쪽이 차감 전 잔액을 읽어 이체 금액만큼 잔액이 늘어나기 때문.
     */
    private fun post(
        fintechUseNum: String,
        direction: TransactionDirection,
        amount: BigDecimal,
        printContent: String,
        counterpartyName: String?,
        at: LocalDateTime,
    ): String {
        val balance = BigDecimal(checkNotNull(accountDao.find(fintechUseNum)).balanceAmt)
        val scale = balance.scale()
        val newBalance = when (direction) {
            TransactionDirection.WITHDRAWAL -> balance - amount
            TransactionDirection.DEPOSIT -> balance + amount
        }.let { format(it, scale) }

        accountDao.updateBalance(fintechUseNum, newBalance)
        transactionDao.insert(
            TransactionRecord(
                fintechUseNum = fintechUseNum,
                tranDate = at.format(DATE_FORMATTER),
                tranTime = at.format(TIME_FORMATTER),
                direction = direction,
                printContent = printContent,
                tranAmt = format(amount, scale),
                afterBalanceAmt = newBalance,
                counterpartyName = counterpartyName,
            ),
        )
        return newBalance
    }

    private fun format(value: BigDecimal, scale: Int): String =
        value.setScale(scale, RoundingMode.HALF_UP).toPlainString()

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    }
}
