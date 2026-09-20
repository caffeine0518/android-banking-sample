package com.study.bank.data.remote.kftc.mock.service

import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import java.math.BigDecimal

/**
 * 출금 요청을 검증한 결과. 거절이거나, 원장에 적용할 입력을 묶은 승인이다.
 *
 * 검증과 적용을 갈라 두려고 둔다 — 부수효과 없는 검증이 이 타입을 만들고, 적용 단계는
 * [Approved]만 받아 계좌를 건드린다([KftcWithdrawalServiceImpl]).
 */
internal sealed interface WithdrawPlan {

    data class Reject(val result: WithdrawResult) : WithdrawPlan

    /** [recipient]가 null이면 외부 이체 — 출금만 하고 입금 상대가 없다. */
    data class Approved(
        val source: SeedAccount,
        val amount: BigDecimal,
        val recipient: SeedAccount?,
    ) : WithdrawPlan
}
