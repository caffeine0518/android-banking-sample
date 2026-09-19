package com.study.bank.e2e.support

import com.study.bank.data.remote.kftc.mock.KftcMockServer

/**
 * 비행기 모드로는 재현되지 않는다 — mock 서버가 프로세스 내 loopback이라 라디오를 꺼도 요청이 성공한다.
 * 서버가 프로세스 전역 @Singleton이라, 복구를 놓치면 장애가 다음 테스트까지 남는다.
 */
internal inline fun KftcMockServer.withNetworkDown(block: () -> Unit) {
    startDroppingConnections()
    try {
        block()
    } finally {
        stopDroppingConnections()
    }
}
