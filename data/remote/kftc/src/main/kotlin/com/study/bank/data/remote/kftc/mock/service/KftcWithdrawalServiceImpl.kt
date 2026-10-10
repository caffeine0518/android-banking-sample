package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawPlan
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
import com.study.bank.data.remote.kftc.mock.storage.entity.SettledWithdrawal
import javax.inject.Inject

/**
 * [KftcWithdrawalService]의 Room 원장 구현. 검증은 [WithdrawPlanner], 반영은 [WithdrawExecutor]가 담당하고
 * 이 클래스는 실행 순서와 트랜잭션 경계만 정한다.
 *
 * 원자성은 [transactionScope]가 보장한다. 멱등 조회·계좌 갱신·원장 삽입·멱등 기록은 하나의 트랜잭션으로 실행돼야 한다.
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
            settled.fingerprint == command.fingerprint() -> settled.response
            else -> WithdrawResult.IdempotencyConflict(command.bankTranId)
        }
    }

    private fun settle(command: WithdrawCommand): WithdrawResult = when (val plan = planner.plan(command)) {
        is WithdrawPlan.Reject -> plan.result
        is WithdrawPlan.Approved -> executor.execute(plan, command).also { success ->
            withdrawalDao.insertSettled(SettledWithdrawal(success, command.fingerprint()))
        }
    }
}
