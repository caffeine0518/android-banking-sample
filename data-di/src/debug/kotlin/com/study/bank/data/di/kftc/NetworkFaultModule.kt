package com.study.bank.data.di.kftc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 네트워크 장애 주입 seam을 그래프에 연결하는 **debug 전용** 모듈.
 *
 * src/debug에만 존재하므로 release 변형엔 [NetworkFaultController] 바인딩은 물론 인터페이스·구현
 * 클래스 자체가 빠진다 → 배포 버전에서 실수로 주입/호출(enableFault)해 네트워크를 망가뜨리는
 * 휴먼에러가 컴파일 단계에서 원천 차단된다. 장애 토글이 필요한 건 debug 앱과 E2E 테스트뿐이다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkFaultModule {

    @Binds
    @Singleton
    internal abstract fun bindNetworkFaultController(
        impl: KftcNetworkFaultController,
    ): NetworkFaultController
}
