package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawPlan
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 출금 요청을 검증해 [WithdrawPlan]으로 변환한다. 원장은 수정하지 않고 계좌 조회만 한다.
 *
 * 거절 방식은 두 가지다. 값을 얻지 못하면 즉시 거절하고(출금계좌·금액),
 * 값은 있지만 규칙을 위반하면 사유를 반환받아 거절한다(잔액·통화).
 */
internal class WithdrawPlanner @Inject constructor(
    private val accountDao: MockAccountDao,
) {

    fun plan(command: WithdrawCommand): WithdrawPlan {
        val source = accountDao.find(command.fintechUseNum)
            ?: return reject(WithdrawResult.UnknownSender(command.fintechUseNum))

        val amount = command.positiveAmountOrNull()
            ?: return reject(WithdrawResult.InvalidAmount(command.tranAmt))

        // 수취계좌가 내 시드에 있으면 내부 이체(복식부기 대상), 없으면(null) 외부 이체.
        val recipient = accountDao.findByAccountNum(command.recvBankCode, command.recvAccountNum)

        insufficientFunds(source, amount)?.let { return reject(it) }
        currencyMismatch(source, recipient)?.let { return reject(it) }

        return WithdrawPlan.Approved(source, amount, recipient)
    }

    /** 숫자이고 0보다 크면 금액을, 아니면 null을 반환한다. */
    private fun WithdrawCommand.positiveAmountOrNull(): BigDecimal? =
        tranAmt.toBigDecimalOrNull()?.takeIf { it.signum() > 0 }

    /** 잔액이 부족하면 거절 사유를, 충분하면 null을 반환한다. */
    private fun insufficientFunds(source: SeedAccount, amount: BigDecimal): WithdrawResult? {
        val balance = BigDecimal(source.balanceAmt)
        if (balance >= amount) return null
        return WithdrawResult.InsufficientFunds(
            balance = source.balanceAmt,
            attempted = amount.toLedgerString(balance.scale()),
        )
    }

    /** 내부 이체인데 통화가 다르면 거절 사유를, 아니면 null을 반환한다. 외부 이체는 상대 통화를 알 수 없어 확인하지 않는다. */
    private fun currencyMismatch(source: SeedAccount, recipient: SeedAccount?): WithdrawResult? =
        if (recipient != null && recipient.currencyCode != source.currencyCode) {
            WithdrawResult.CurrencyMismatch(source.currencyCode, recipient.currencyCode)
        } else {
            null
        }

    private fun reject(result: WithdrawResult) = WithdrawPlan.Reject(result)
}
