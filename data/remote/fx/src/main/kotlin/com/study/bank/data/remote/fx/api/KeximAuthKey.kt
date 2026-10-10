package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/** 테스트는 인증 실패를 검증하려고 주 생성자로 임의 값을 넣는다. */
@Singleton
class KeximAuthKey(val value: String) {

    @Inject
    constructor() : this(BuildConfig.KEXIM_API_KEY)
}
