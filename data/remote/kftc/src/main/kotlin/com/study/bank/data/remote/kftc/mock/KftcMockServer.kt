package com.study.bank.data.remote.kftc.mock

import com.study.bank.data.remote.kftc.mock.dispatcher.AccountRequestHandler
import com.study.bank.data.remote.kftc.mock.dispatcher.InquiryRequestHandler
import com.study.bank.data.remote.kftc.mock.dispatcher.KftcMockDispatcher
import com.study.bank.data.remote.kftc.mock.dispatcher.KftcMockResponses
import com.study.bank.data.remote.kftc.mock.dispatcher.TransferRequestHandler
import com.study.bank.data.remote.kftc.network.NetworkJson
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * Lifecycle wrapper for the KFTC v2.0 mock server.
 *
 * Lets [com.study.bank.data.remote.kftc.api.KftcApiService] exercise the real network stack so
 * interceptors, serialization, and error paths are all verified in-process. Serves HTTPS with a
 * self-signed loopback certificate so the manifest stays cleartext-free; clients use
 * [clientCertificates] to trust this CA.
 */
@Singleton
class KftcMockServer @Inject constructor(
    networkJson: NetworkJson,
) {

    private val server: MockWebServer = MockWebServer()
    // Composition root: 디스패처/핸들러가 직접 협력자를 생성하지 않도록 여기서 그래프를 명시 조립해 주입한다.
    // responses는 단일 인스턴스를 공유해야 api_tran_id 시퀀스가 엔드포인트 전역으로 1씩 증가한다.
    private val state = KftcBankState(KftcAccountSeed.accounts)
    private val responses = KftcMockResponses()
    private val dispatcher = KftcMockDispatcher(
        accountHandler = AccountRequestHandler(state, responses),
        transferHandler = TransferRequestHandler(
            state,
            responses,
            networkJson.value,
            responseDelayMillis = WITHDRAW_RESPONSE_DELAY_MS,
        ),
        inquiryHandler = InquiryRequestHandler(
            KftcRecipientSeed.directory(KftcAccountSeed.accounts),
            responses,
            networkJson.value,
        ),
        responses = responses,
    )
    private val localhostCertificate: HeldCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    val clientCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localhostCertificate.certificate)
        .build()

    private var started: Boolean = false

    init {
        start()
    }

    fun start() {
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

    fun baseUrl(): HttpUrl {
        check(started) { "KftcMockServer가 아직 start되지 않았다" }
        // server.url("/")은 canonicalHostName(역DNS)을 타 메인 스레드 네트워크가 된다. 바인딩과 같은 IPv4로 직접 구성.
        return HttpUrl.Builder()
            .scheme("https")
            .host(LOOPBACK_HOST)
            .port(server.port)
            .build()
    }

    fun shutdown() {
        if (!started) return
        server.shutdown()
        started = false
    }

    /** 테스트 전용: 가장 오래된 수신 요청 1건을 큐에서 꺼낸다. */
    internal fun takeRequest(timeoutMs: Long = 1_000): RecordedRequest? =
        server.takeRequest(timeoutMs, TimeUnit.MILLISECONDS)

    /**
     * 테스트 전용: 이후 모든 요청을 서버 장애(5xx)로 응답하게 한다.
     * @Singleton이라 주입받은 인스턴스가 곧 API가 호출하는 서버이므로, 이 토글로 실 네트워크 실패를 재현한다.
     */
    fun enableFault() {
        dispatcher.faultEnabled = true
    }

    /** 테스트 전용: [enableFault]로 켠 장애 주입을 해제한다. */
    fun disableFault() {
        dispatcher.faultEnabled = false
    }

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"
        // 이름 해석 없이 IPv4 루프백 생성(메인 스레드 안전).
        val LOOPBACK_ADDRESS: InetAddress = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
        // 데모/수동 테스트용: 송금 응답을 지연시켜 "보내는 중이에요" 로딩 화면이 최소 1초 보이게 한다.
        const val WITHDRAW_RESPONSE_DELAY_MS = 1_000L
    }
}
