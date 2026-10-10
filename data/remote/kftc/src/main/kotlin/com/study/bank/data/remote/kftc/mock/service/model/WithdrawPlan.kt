package com.study.bank.data.remote.kftc.mock.service.model

import com.study.bank.data.remote.kftc.mock.storage.entity.SeedAccount
import java.math.BigDecimal

/**
 * 출금 요청의 검증 결과. 거절이거나, 원장에 반영할 입력을 모은 승인이다.
 *
 * 검증과 반영을 분리하기 위한 타입이다. 부수 효과 없는 검증이 이 타입을 만들고, 반영 단계는
 * [Approved]만 받아 계좌를 수정한다([KftcWithdrawalServiceImpl]).
 */
internal sealed interface WithdrawPlan {

    data class Reject(val result: WithdrawResult) : WithdrawPlan

    /** [recipient]가 null이면 외부 이체다. 출금만 하고 입금할 계좌가 없다. */
    data class Approved(
        val source: SeedAccount,
        val amount: BigDecimal,
        val recipient: SeedAccount?,
    ) : WithdrawPlan
}
