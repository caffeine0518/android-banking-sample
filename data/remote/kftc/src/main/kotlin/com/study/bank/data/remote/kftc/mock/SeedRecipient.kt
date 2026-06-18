package com.study.bank.data.remote.kftc.mock

/**
 * 계좌실명조회(inquiry/real_name)가 (bankCodeStd, accountNum)으로 매칭하는 수취 계좌 한 건.
 *
 * 본인 명의 계좌는 자기/내부 이체 판별을 위해 그 계좌의 fintech_use_num을 [accountId]로 갖고,
 * 외부 수취인은 합성 id를 갖는다. [active]=false는 휴면/해지 → RecipientLookup.Inactive로 매핑된다.
 */
internal data class SeedRecipient(
    val bankCodeStd: String,
    val accountNum: String,
    val accountId: String,
    val holderName: String,
    val active: Boolean,
)
