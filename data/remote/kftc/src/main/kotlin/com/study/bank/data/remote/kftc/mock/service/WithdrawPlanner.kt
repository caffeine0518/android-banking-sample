package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawPlan
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import java.math.BigDecimal
import javax.inject.Inject

internal class WithdrawPlanner @Inject constructor(
    private val accountDao: MockAccountDao,
) {

    fun plan(command: WithdrawCommand): WithdrawPlan {
        val source = accountDao.find(command.fintechUseNum)
            ?: return reject(WithdrawResult.UnknownSender(command.fintechUseNum))

        val amount = command.positiveAmountOrNull()
            ?: return reject(WithdrawResult.InvalidAmount(command.tranAmt))

        // null이면 외부 이체.
        val recipient = accountDao.findByAccountNum(command.recvBankCode, command.recvAccountNum)

        insufficientFunds(source, amount)?.let { return reject(it) }
        currencyMismatch(source, recipient)?.let { return reject(it) }

        return WithdrawPlan.Approved(source, amount, recipient)
    }

    private fun WithdrawCommand.positiveAmountOrNull(): BigDecimal? =
        tranAmt.toBigDecimalOrNull()?.takeIf { it.signum() > 0 }

    private fun insufficientFunds(source: SeedAccount, amount: BigDecimal): WithdrawResult? {
        val balance = BigDecimal(source.balanceAmt)
        if (balance >= amount) return null
        return WithdrawResult.InsufficientFunds(
            balance = source.balanceAmt,
            attempted = amount.toLedgerString(balance.scale()),
        )
    }

    /** 외부 이체는 상대 통화를 알 수 없어 확인하지 않는다. */
    private fun currencyMismatch(source: SeedAccount, recipient: SeedAccount?): WithdrawResult? =
        if (recipient != null && recipient.currencyCode != source.currencyCode) {
            WithdrawResult.CurrencyMismatch(source.currencyCode, recipient.currencyCode)
        } else {
            null
        }

    private fun reject(result: WithdrawResult) = WithdrawPlan.Reject(result)
}
