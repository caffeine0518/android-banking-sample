package com.study.bank.data.remote.kftc.mock

import okhttp3.HttpUrl
import okhttp3.tls.HandshakeCertificates

interface KftcMockServer {

    /** 서버가 HTTPS로 응답하므로 클라이언트는 이 인증서를 신뢰해야 한다. */
    val clientCertificates: HandshakeCertificates

    /** 실행 중인 서버 주소. [start] 전에 호출하면 실패한다. */
    fun baseUrl(): HttpUrl

    /** 이미 실행 중이면 아무것도 하지 않는다. */
    fun start()

    /** 실행 중이 아니면 아무것도 하지 않는다. */
    fun shutdown()

    /**
     * 이후 요청의 **응답만** 유실시킨다. 서버 상태는 이미 반영되므로, 응답을 받지 못한 클라이언트가
     * 재시도하는 상황(이중 출금 위험)을 재현한다.
     */
    fun startDroppingConnections()

    fun stopDroppingConnections()
}
