package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionScopeDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockWithdrawalDao
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
        withdrawalDao.find(command.bankTranId)
            ?: when (val plan = planner.plan(command)) {
                is WithdrawPlan.Reject -> plan.result
                is WithdrawPlan.Approved -> executor.execute(plan, command).also(withdrawalDao::insert)
            }
    }
}
