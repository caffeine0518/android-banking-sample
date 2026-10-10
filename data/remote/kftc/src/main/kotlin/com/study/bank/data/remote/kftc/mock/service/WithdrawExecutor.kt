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

/** 출금과 입금이 함께 반영돼야 하므로 호출 측의 트랜잭션 안에서만 호출한다. */
internal class WithdrawExecutor @Inject constructor(
    private val accountDao: MockAccountDao,
    private val transactionDao: MockTransactionDao,
    private val clock: Clock,
) {

    fun execute(plan: WithdrawPlan.Approved, command: WithdrawCommand): WithdrawResult.Success {
        val (source, amount, recipient) = plan
        val at = LocalDateTime.now(clock)

        val afterSource = withdrawFrom(source, amount, command, at)
        if (recipient != null) {
            depositTo(recipient, amount, command, at)
        }
        return success(source, amount, command, afterSource)
    }

    /** 인자내용이 없으면 상대방 이름을 쓴다(KFTC 기본 동작). */
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

    /** 같은 계좌로 송금하면(출금 = 수취) 입금 쪽이 차감 전 잔액을 쓰지 않도록 잔액을 매번 다시 조회한다. */
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

/** 이 모듈은 :domain의 Currency를 몰라 통화별 자릿수 대신 저장된 scale을 유지한다. */
internal fun BigDecimal.toLedgerString(scale: Int): String =
    setScale(scale, RoundingMode.HALF_UP).toPlainString()
