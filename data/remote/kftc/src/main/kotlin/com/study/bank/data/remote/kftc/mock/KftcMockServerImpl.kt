package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.http.dispatcher.KftcMockDispatcher
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate

/**
 * [KftcMockServer]를 MockWebServer로 구현한다.
 *
 * 라우팅과 응답은 주입받은 [dispatcher]가 처리하고, 이 클래스는 수명 주기와 TLS만 담당한다.
 * 매니페스트에 cleartext 허용을 두지 않으려고 자체 서명 loopback 인증서로 HTTPS를 제공한다.
 * 생성 시 바로 [start]하므로 주입받은 시점에는 이미 실행 중이다.
 */
@Singleton
internal class KftcMockServerImpl @Inject constructor(
    private val dispatcher: KftcMockDispatcher,
) : KftcMockServer {

    private val server = MockWebServer()

    private val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    override val clientCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localhostCertificate.certificate)
        .build()

    private var started: Boolean = false

    init {
        start()
    }

    override fun start() {
        if (started) return
        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(localhostCertificate)
            .build()
        server.useHttps(serverCertificates.sslSocketFactory(), false)
        server.dispatcher = dispatcher
        // 인자 없는 start()는 getByName("localhost")로 이름을 해석해 메인 스레드에서 NetworkOnMainThreadException이 발생한다.
        // IPv4 주소 바이트로 바인딩해 이름 해석을 생략하고, localhost가 ::1로 해석되는 문제도 피한다.
        server.start(LOOPBACK_ADDRESS, 0)
        started = true
    }

    override fun baseUrl(): HttpUrl {
        check(started) { "KftcMockServer가 아직 start되지 않았다" }
        // server.url("/")은 canonicalHostName(역방향 DNS)을 조회해 메인 스레드에서 네트워크를 쓴다. 바인딩한 IPv4 주소로 직접 구성한다.
        return HttpUrl.Builder()
            .scheme("https")
            .host(LOOPBACK_HOST)
            .port(server.port)
            .build()
    }

    override fun shutdown() {
        if (!started) return
        server.shutdown()
        started = false
    }

    /**
     * 테스트 전용: 수신한 요청 중 가장 오래된 1건을 반환한다.
     * 인터페이스 멤버는 internal로 선언할 수 없어 구현체에만 둔다. 호출 측은 이 타입을 직접 생성한다.
     */
    internal fun takeRequest(timeoutMs: Long = 1_000): RecordedRequest? =
        server.takeRequest(timeoutMs, TimeUnit.MILLISECONDS)

    override fun startDroppingConnections() {
        dispatcher.dropConnections = true
    }

    override fun stopDroppingConnections() {
        dispatcher.dropConnections = false
    }

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"
        // 이름을 해석하지 않으므로 메인 스레드에서도 안전하다.
        val LOOPBACK_ADDRESS: InetAddress = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
    }
}
