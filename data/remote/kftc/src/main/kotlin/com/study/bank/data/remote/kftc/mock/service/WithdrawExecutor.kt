package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawPlan
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.entity.TransactionRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 승인된 [WithdrawPlan.Approved]를 원장에 반영한다. 검증은 [WithdrawPlanner]가 이미 마쳤다.
 *
 * 출금과 입금이 함께 반영돼야 하므로 호출 측([KftcWithdrawalServiceImpl])의 트랜잭션 안에서만 호출한다.
 */
internal class WithdrawExecutor @Inject constructor(
    private val accountDao: MockAccountDao,
    private val transactionDao: MockTransactionDao,
    private val clock: Clock,
) {

    /** 출금계좌에서 차감하고, 내부 수취면 같은 시각으로 입금까지 기록한다. 외부 수취면 차감만 한다. */
    fun execute(plan: WithdrawPlan.Approved, command: WithdrawCommand): WithdrawResult.Success {
        val (source, amount, recipient) = plan
        val at = LocalDateTime.now(clock)

        val afterSource = withdrawFrom(source, amount, command, at)
        if (recipient != null) {
            depositTo(recipient, amount, command, at)
        }
        return success(source, amount, command, afterSource)
    }

    /** 통장 인자내용은 요청 값을 쓰고, 없으면 상대방 이름을 쓴다(KFTC 기본 동작). */
    private fun withdrawFrom(
        source: SeedAccount,
        amount: BigDecimal,
        command: WithdrawCommand,
        at: LocalDateTime,
    ): String = post(
        fintechUseNum = source.fintechUseNum,
        direction = TransactionDirection.WITHDRAWAL,
        amount = amount,
        printContent = command.wdPrintContent ?: command.recvName,
        counterpartyName = command.recvName,
        at = at,
    )

    private fun depositTo(
        recipient: SeedAccount,
        amount: BigDecimal,
        command: WithdrawCommand,
        at: LocalDateTime,
    ) {
        post(
            fintechUseNum = recipient.fintechUseNum,
            direction = TransactionDirection.DEPOSIT,
            amount = amount,
            printContent = command.dpsPrintContent ?: command.reqName,
            counterpartyName = command.reqName,
            at = at,
        )
    }

    private fun success(
        source: SeedAccount,
        amount: BigDecimal,
        command: WithdrawCommand,
        afterBalanceAmt: String,
    ) = WithdrawResult.Success(
        bankTranId = command.bankTranId,
        fintechUseNum = source.fintechUseNum,
        bankCodeStd = source.bankCodeStd,
        accountNumMasked = source.accountNumMasked,
        accountHolderName = source.accountHolderName,
        tranAmt = amount.toLedgerString(BigDecimal(source.balanceAmt).scale()),
        afterBalanceAmt = afterBalanceAmt,
    )

    /**
     * 한 계좌에 입금 또는 출금 1건을 반영한다. 잔액을 [direction]에 따라 갱신하고 원장에 한 행을 추가한 뒤,
     * 갱신된 잔액 문자열을 반환한다.
     *
     * 잔액은 스냅샷을 쓰지 않고 **매번 테이블에서 다시 조회한다**. 그렇지 않으면 같은 계좌로 송금할 때(출금 = 수취)
     * 입금 쪽이 차감 전 잔액을 읽어 이체 금액만큼 잔액이 늘어난다.
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
        }.toLedgerString(scale)

        accountDao.updateBalance(fintechUseNum, newBalance)
        transactionDao.insert(
            TransactionRecord(
                fintechUseNum = fintechUseNum,
                tranDate = at.format(DATE_FORMATTER),
                tranTime = at.format(TIME_FORMATTER),
                direction = direction,
                printContent = printContent,
                tranAmt = amount.toLedgerString(scale),
                afterBalanceAmt = newBalance,
                counterpartyName = counterpartyName,
            ),
        )
        return newBalance
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    }
}

/** 원장 문자열은 저장된 scale을 그대로 유지한다. 이 모듈은 :domain의 Currency를 참조하지 않아 통화별 자릿수를 모른다. */
internal fun BigDecimal.toLedgerString(scale: Int): String =
    setScale(scale, RoundingMode.HALF_UP).toPlainString()
