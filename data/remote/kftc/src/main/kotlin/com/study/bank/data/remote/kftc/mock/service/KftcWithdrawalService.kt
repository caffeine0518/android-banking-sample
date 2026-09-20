package com.study.bank.data.remote.kftc.mock.service

/**
 * Mock 은행의 출금이체 — 실제 KFTC 뒤에 있을 코어뱅킹에 해당한다.
 *
 * 조회는 핸들러가 DAO를 직접 쓰고, 여기엔 쓰기 규칙만 둔다: 검증 → 복식부기 → 멱등 기록.
 */
internal interface KftcWithdrawalService {

    /**
     * 출금이체. 같은 [WithdrawCommand.bankTranId]로 이미 체결됐으면 원장을 변경하지 않고 그 응답을 반환한다.
     * 거절 건은 기록하지 않는다 — 원장을 변경하지 않았으므로 같은 키로 다시 시도할 수 있다.
     */
    fun withdraw(command: WithdrawCommand): WithdrawResult
}
