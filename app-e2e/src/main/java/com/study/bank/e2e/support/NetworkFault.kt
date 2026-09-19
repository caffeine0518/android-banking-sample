package com.study.bank.e2e.support

import com.study.bank.data.remote.kftc.mock.KftcMockServer

/** 블록 안의 요청을 서버가 처리한 뒤 응답만 유실시킨다(연결 차단) — 요청을 무시하는 게 아니다. */
internal inline fun KftcMockServer.withNetworkDown(block: () -> Unit) {
    startDroppingConnections()
    try {
        block()
    } finally {
        stopDroppingConnections()
    }
}
