package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult

/**
 * Mock 은행의 출금이체. 실제 KFTC 뒤에 있는 코어뱅킹에 해당한다.
 *
 * 조회는 핸들러가 DAO로 직접 하고, 여기에는 쓰기 규칙(검증 → 복식부기 → 멱등 기록)만 둔다.
 */
internal interface KftcWithdrawalService {

    /**
     * 출금이체. 같은 [WithdrawCommand.bankTranId]로 이미 체결됐으면 원장을 변경하지 않고 그 응답을 반환한다.
     * 단, 출금계좌·금액·수취계좌가 다르면 같은 거래가 아니므로 [WithdrawResult.IdempotencyConflict]로 거절한다.
     * 거절 건은 원장을 변경하지 않았으므로 기록하지 않고, 같은 키로 다시 시도할 수 있다.
     */
    fun withdraw(command: WithdrawCommand): WithdrawResult
}
