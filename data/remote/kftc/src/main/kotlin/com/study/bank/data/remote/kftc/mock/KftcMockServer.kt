package com.study.bank.data.remote.kftc.mock

import okhttp3.HttpUrl
import okhttp3.tls.HandshakeCertificates

/**
 * KFTC v2.0 mock 서버 인터페이스. 서버 수명 주기와 네트워크 장애 주입을 제공한다.
 *
 * [com.study.bank.data.remote.kftc.api.KftcApiService]가 실제 네트워크 스택을 그대로 거치므로
 * 인터셉터·직렬화·오류 처리까지 프로세스 안에서 검증할 수 있다. 구현이 HTTPS로 응답하므로 클라이언트는
 * [clientCertificates]로 서버 인증서를 신뢰해야 한다.
 */
interface KftcMockServer {

    /** 이 서버의 인증서를 신뢰하는 클라이언트 측 인증서 묶음. */
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
