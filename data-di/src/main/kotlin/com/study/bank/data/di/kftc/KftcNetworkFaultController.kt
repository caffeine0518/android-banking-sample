package com.study.bank.data.di.kftc

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkFaultController]를 KFTC mock 서버에 위임하는 어댑터.
 *
 * @Singleton인 [KftcMockServer]를 주입받으므로 토글 대상이 곧 API가 호출하는 그 서버다.
 * mock 의존을 data-di 안에 가둬, :app은 이 인터페이스만 보면 된다.
 */
@Singleton
internal class KftcNetworkFaultController @Inject constructor(
    private val server: KftcMockServer,
) : NetworkFaultController {

    override fun enableFault() = server.enableFault()

    override fun disableFault() = server.disableFault()
}
