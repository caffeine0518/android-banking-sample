package com.study.bank.data.remote.fx.api

import javax.inject.Qualifier

/**
 * KEXIM API 인증키 의존성 식별용 JSR-330 qualifier.
 *
 * 외부에서 `BuildConfig.KEXIM_API_KEY` 같은 String을 이 qualifier로 노출하면
 * [KeximApiServiceImpl]의 생성자 주입이 정확히 그 String을 받는다.
 * `@Named("...")` 같은 string-typed 키 회피용.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KeximAuthKey
