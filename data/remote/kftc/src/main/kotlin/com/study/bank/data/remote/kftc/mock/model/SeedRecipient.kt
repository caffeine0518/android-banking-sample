package com.study.bank.data.remote.kftc.mock.model

import com.study.bank.data.remote.kftc.mock.seed.KftcSeedAccountIds

internal data class SeedRecipient(
    val bankCodeStd: String,
    /** 하이픈을 포함해도 된다. 비교할 때 숫자만 남긴다. */
    val accountNum: String,
    /**
     * 앱은 출금계좌 id와 같으면 같은 계좌로 보내는 송금으로 보고 차단한다. 그래서 본인 계좌는 계좌 목록의
     * fintech_use_num을, 외부 수취인은 겹치지 않는 값("ext-088-…")을 쓴다.
     */
    val accountId: String,
    val holderName: String,
    /** false면 휴면·해지 계좌다. */
    val active: Boolean,
)
