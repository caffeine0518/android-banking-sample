package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawPlan
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import java.math.BigDecimal
import javax.inject.Inject

/**
 * [KftcWithdrawalService]를 Room 원장 위에 구현한다. 검증은 [WithdrawPlanner], 적용은
 * [WithdrawExecutor]가 맡고 여기는 순서와 경계만 잡는다.
 *
 * 원자성은 [transactionScope]가 보장한다 — 멱등 조회·계좌 갱신·원장 삽입·멱등 기록이 한 덩어리여야 한다.
 */
internal class KftcWithdrawalServiceImpl @Inject constructor(
    private val transactionScope: MockTransactionScopeDao,
    private val planner: WithdrawPlanner,
    private val executor: WithdrawExecutor,
    private val withdrawalDao: MockWithdrawalDao,
) : KftcWithdrawalService {

    override fun withdraw(command: WithdrawCommand): WithdrawResult = transactionScope.inTransaction {
        val settled = withdrawalDao.findSettled(command.bankTranId)
        when {
            settled == null -> settle(command)
            settled.isSameTransfer(command) -> settled
            else -> WithdrawResult.IdempotencyConflict(command.bankTranId)
        }
    }

    private fun settle(command: WithdrawCommand): WithdrawResult = when (val plan = planner.plan(command)) {
        is WithdrawPlan.Reject -> plan.result
        is WithdrawPlan.Approved -> executor.execute(plan, command).also(withdrawalDao::insertSettled)
    }

    /** 금액은 저장 시 scale 이 맞춰지므로 문자열이 아니라 값으로 비교한다("50000" 과 "50000.00" 은 같은 금액). */
    private fun WithdrawResult.Success.isSameTransfer(command: WithdrawCommand): Boolean =
        fintechUseNum == command.fintechUseNum &&
            recvAccountNum == command.recvAccountNum &&
            recvBankCode == command.recvBankCode &&
            command.tranAmt.toBigDecimalOrNull()?.compareTo(BigDecimal(tranAmt)) == 0
}
