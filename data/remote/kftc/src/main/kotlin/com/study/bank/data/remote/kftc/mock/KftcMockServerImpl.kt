package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.http.KftcMockDispatcher
import com.study.bank.data.remote.kftc.mock.http.handler.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.http.handler.TransferRequestHandler
import com.study.bank.data.remote.kftc.mock.http.kftcRoutes
import com.study.bank.data.remote.kftc.mock.http.response.KftcEnvelopes
import com.study.bank.data.remote.kftc.mock.seed.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.seed.KftcRecipientSeed
import com.study.bank.data.remote.kftc.mock.service.KftcWithdrawalService
import com.study.bank.data.remote.kftc.mock.storage.dao.MockAccountDao
import com.study.bank.data.remote.kftc.mock.storage.dao.MockTransactionDao
import com.study.bank.data.remote.kftc.network.NetworkJson
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
 * 매니페스트에 cleartext 허용을 남기지 않으려고 자체 서명 loopback 인증서로 HTTPS를 제공한다.
 * 생성 즉시 [start]하므로 주입받은 시점에 이미 실행 중이다.
 */
@Singleton
internal class KftcMockServerImpl @Inject constructor(
    accountDao: MockAccountDao,
    transactionDao: MockTransactionDao,
    withdrawalService: KftcWithdrawalService,
    networkJson: NetworkJson,
) : KftcMockServer {

    private val server: MockWebServer = MockWebServer()
    private val envelopes = KftcEnvelopes()
    private val dispatcher = KftcMockDispatcher(
        routes = kftcRoutes(
            account = AccountRequestHandler(accountDao, transactionDao, envelopes),
            transfer = TransferRequestHandler(
                withdrawalService,
                envelopes,
                networkJson.value,
                responseDelayMillis = WITHDRAW_RESPONSE_DELAY_MS,
            ),
            inquiry = InquiryRequestHandler(
                KftcRecipientSeed.directory(KftcAccountSeed.accounts),
                envelopes,
                networkJson.value,
            ),
        ),
        envelopes = envelopes,
    )
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
        // start()는 getByName("localhost")로 이름 해석 → 메인 스레드면 NetworkOnMainThreadException.
        // raw IPv4 바이트로 바인딩해 해석 없이(메인 스레드 안전) ::1 매칭까지 회피한다.
        server.start(LOOPBACK_ADDRESS, 0)
        started = true
    }

    override fun baseUrl(): HttpUrl {
        check(started) { "KftcMockServer가 아직 start되지 않았다" }
        // server.url("/")은 canonicalHostName(역DNS)을 타 메인 스레드 네트워크가 된다. 바인딩과 같은 IPv4로 직접 구성.
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
     * 테스트 전용: 큐에 쌓인 수신 요청 중 가장 오래된 1건을 반환한다.
     * 인터페이스 멤버는 internal일 수 없어 구현체에만 둔다 — 호출 측은 이 타입으로 직접 생성한다.
     */
    internal fun takeRequest(timeoutMs: Long = 1_000): RecordedRequest? =
        server.takeRequest(timeoutMs, TimeUnit.MILLISECONDS)

    /** @Singleton이라 주입받은 인스턴스가 곧 API가 호출하는 그 서버다. */
    override fun startDroppingConnections() {
        dispatcher.dropConnections = true
    }

    override fun stopDroppingConnections() {
        dispatcher.dropConnections = false
    }

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"
        // 이름 해석 없이 IPv4 루프백 생성(메인 스레드 안전).
        val LOOPBACK_ADDRESS: InetAddress = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
        // 데모/수동 테스트용: 송금 응답을 지연시켜 "보내는 중이에요" 로딩 화면이 최소 1초 보이게 한다.
        const val WITHDRAW_RESPONSE_DELAY_MS = 1_000L
    }
}
