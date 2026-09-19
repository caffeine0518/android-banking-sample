package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * KEXIM API 인증키 홀더.
 *
 * 운영 경로는 [BuildConfig.KEXIM_API_KEY]를 읽는 [Inject] 생성자로 주입된다.
 * 테스트는 인증 실패 경로를 검증하려고 주 생성자에 임의 값을 직접 넣어 생성한다.
 */
@Singleton
class KeximAuthKey(val value: String) {

    @Inject
    constructor() : this(BuildConfig.KEXIM_API_KEY)
}
