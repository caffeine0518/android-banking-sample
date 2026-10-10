package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.service.model.WithdrawCommand
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult

internal interface KftcWithdrawalService {

    /**
     * 같은 [WithdrawCommand.bankTranId]로 이미 체결됐으면 원장을 변경하지 않고 그 응답을 반환한다.
     * 출금계좌·금액·수취계좌가 다르면 [WithdrawResult.IdempotencyConflict]로 거절한다.
     * 거절 건은 기록하지 않으므로 같은 키로 다시 시도할 수 있다.
     */
    fun withdraw(command: WithdrawCommand): WithdrawResult
}
