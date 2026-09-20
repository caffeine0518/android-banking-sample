package com.study.bank.data.remote.kftc.mock.model

import com.study.bank.data.remote.kftc.mock.seed.KftcSeedAccountIds

/**
 * 계좌실명조회(inquiry/real_name)가 돌려줄 수취 계좌 한 건.
 *
 * 앱이 받는 사람의 은행·계좌번호를 보내면 mock이 이 목록에서 맞는 건을 찾아 예금주명과 상태를 돌려준다.
 */
internal data class SeedRecipient(
    /** 은행 표준코드. 토스뱅크는 "092", 신한은행은 "088". */
    val bankCodeStd: String,
    /** 계좌번호. 하이픈을 넣어 적어도 되며, 비교할 때 양쪽에서 숫자만 남겨 맞춘다. */
    val accountNum: String,
    /**
     * 이 계좌를 가리키는 번호(응답의 `account_id`) — 오픈뱅킹이 계좌마다 발급하는 24자리 값
     * ([KftcSeedAccountIds]). 앱은 보내는 계좌와 이 번호가 같으면 송금을 막는다(같은 계좌로 보내기.
     * 내 다른 계좌로 보내는 것은 번호가 달라 통과한다). 그래서 본인 계좌면 계좌 목록과 같은 번호를,
     * 외부 수취인이면 겹칠 수 없는 값("ext-088-…")을 쓴다.
     */
    val accountId: String,
    /** 예금주명. 실명조회 결과로 송금 화면에 표시된다. */
    val holderName: String,
    /** false면 휴면·해지 계좌. 응답 상태가 INACTIVE로 나가고 앱이 송금을 막는다. */
    val active: Boolean,
)
