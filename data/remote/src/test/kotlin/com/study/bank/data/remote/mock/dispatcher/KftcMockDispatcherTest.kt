package com.study.bank.data.remote.mock.dispatcher

import com.study.bank.data.remote.mock.KftcAccountSeed
import com.study.bank.data.remote.mock.SeedAccount
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [KftcMockDispatcher] 라우팅/에러 분기 단독 검증.
 *
 * Retrofit/DTO 직렬화 경로는 [com.study.bank.data.remote.api.KftcApiServiceTest]에서 다루므로
 * 여기선 raw HTTP body 문자열만 보고 dispatcher 책임(경로 매칭, 시드 조회, [MockError] 분기)에 집중한다.
 */
class KftcMockDispatcherTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply {
            dispatcher = KftcMockDispatcher()
            start()
        }
        client = OkHttpClient()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // happy path baseline — 첫 라우트가 200 + envelope 골격을 그대로 돌려주는지.
    @Test
    fun `list_finuse는 200과 KFTC 성공 envelope을 돌려준다`() {
        val (code, body) = get("/v2.0/account/list_finuse")

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("res_list 들어있어야 함: $body", body.contains(""""res_list""""))
        assertTrue("res_cnt가 기본 시드 크기와 일치: $body",
            body.contains(""""res_cnt":"${KftcAccountSeed.accounts.size}""""))
    }

    // seed lookup 정확성 — fintechUseNum 키로 매칭된 계좌의 잔액/통화가 응답에 그대로 흘러가는지.
    @Test
    fun `balance fin_num + 유효한 fintech_use_num은 200과 해당 시드 잔액을 돌려준다`() {
        val krw = KftcAccountSeed.accounts.firstOrNull { it.currencyCode == "KRW" }
            ?: error("이 테스트는 기본 시드에 KRW 계좌가 최소 1개 있다고 가정한다")

        val (code, body) = get("/v2.0/account/balance/fin_num?fintech_use_num=${krw.fintechUseNum}")

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("시드 잔액과 일치: $body", body.contains(""""balance_amt":"${krw.balanceAmt}""""))
        assertTrue("시드 통화코드와 일치: $body", body.contains(""""currency_code":"${krw.currencyCode}""""))
    }

    // MissingFintechUseNum 분기 — isNullOrBlank의 null 가지 (쿼리 자체가 없는 경우).
    @Test
    fun `fintech_use_num 쿼리 누락은 400과 MissingFintechUseNum 메시지`() {
        val (code, body) = get("/v2.0/account/balance/fin_num")

        assertEquals(400, code)
        assertTrue("MockError MissingFintechUseNum 메시지: $body",
            body.contains("fintech_use_num 쿼리 누락"))
    }

    // MissingFintechUseNum 분기 — isNullOrBlank의 blank 가지. 입력 형태가 위와 달라서 분리.
    @Test
    fun `fintech_use_num 쿼리가 빈 값이어도 동일 분기로 400`() {
        val (code, body) = get("/v2.0/account/balance/fin_num?fintech_use_num=")

        assertEquals(400, code)
        assertTrue("MockError MissingFintechUseNum 메시지: $body",
            body.contains("fintech_use_num 쿼리 누락"))
    }

    // UnknownFintechUseNum — 404 + 메시지에 입력값 echo (디버깅 가능성 보장).
    @Test
    fun `존재하지 않는 fintech_use_num은 404와 입력값을 포함한 메시지`() {
        val bogus = "999999999999999999999999"

        val (code, body) = get("/v2.0/account/balance/fin_num?fintech_use_num=$bogus")

        assertEquals(404, code)
        assertTrue("UnknownFintechUseNum 메시지에 입력값 포함: $body", body.contains(bogus))
    }

    // UnknownEndpoint — when else 분기. 새 라우트 추가 누락 시 회귀 잡힘.
    @Test
    fun `등록되지 않은 path는 404와 UnknownEndpoint 메시지 + 해당 path 포함`() {
        val (code, body) = get("/v2.0/garbage")

        assertEquals(404, code)
        assertTrue("입력 path 포함: $body", body.contains("/v2.0/garbage"))
    }

    // KFTC 스펙 약속: 에러도 envelope 형식 유지. 빠지면 클라 파싱 실패.
    @Test
    fun `에러 응답도 KFTC envelope 4필드와 A0001 rsp_code를 모두 채운다`() {
        val (_, body) = get("/v2.0/garbage")

        assertEnvelope(body, rspCode = "A0001")
    }

    // 생성자 seed 파라미터 DI 동작. dispatcher가 기본 seed 직접 참조하는 회귀를 잡음.
    @Test
    fun `커스텀 seed 주입 시 list_finuse는 그 seed만 돌려준다`() {
        server.dispatcher = KftcMockDispatcher(
            seed = listOf(
                SeedAccount(
                    fintechUseNum = "TESTFINNUM0000001",
                    bankCodeStd = "092",
                    bankName = "토스뱅크",
                    accountNumMasked = "100-***-001",
                    accountAlias = "테스트통장",
                    accountHolderName = "테스터",
                    accountType = "1",
                    balanceAmt = "100",
                    currencyCode = "KRW",
                    productName = "테스트상품",
                ),
            ),
        )

        val (code, body) = get("/v2.0/account/list_finuse")

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("res_cnt가 시드 크기와 일치: $body", body.contains(""""res_cnt":"1""""))
        assertTrue("주입한 fintech_use_num이 응답에 들어있음: $body", body.contains("TESTFINNUM0000001"))
        assertTrue("주입한 별칭이 응답에 들어있음: $body", body.contains("테스트통장"))
    }

    // AtomicLong 카운터 +1 정확성. 더블 호출/미증가/카운터 공유 회귀를 모두 잡음.
    @Test
    fun `연속 호출의 api_tran_id는 정확히 1씩 증가한다`() {
        val firstBody = get("/v2.0/account/list_finuse").second
        val secondBody = get("/v2.0/account/list_finuse").second

        val firstSeq = extractTranSeq(firstBody)
        val secondSeq = extractTranSeq(secondBody)

        assertEquals(firstSeq + 1, secondSeq)
    }

    private fun get(path: String): Pair<Int, String> {
        val response = client.newCall(
            Request.Builder().url(server.url(path)).build(),
        ).execute()
        return response.use { it.code to (it.body?.string().orEmpty()) }
    }

    private fun assertEnvelope(body: String, rspCode: String) {
        assertTrue("api_tran_id 채움: $body", body.contains(""""api_tran_id""""))
        assertTrue("api_tran_dtm 채움: $body", body.contains(""""api_tran_dtm""""))
        assertTrue("rsp_code $rspCode: $body", body.contains(""""rsp_code":"$rspCode""""))
        assertTrue("rsp_message 키 존재: $body", body.contains(""""rsp_message""""))
    }

    private fun extractTranSeq(body: String): Long {
        val tranId = TRAN_ID_REGEX.find(body)?.groupValues?.get(1)
            ?: error("api_tran_id 추출 실패: $body")
        return SEQ_DIGITS_REGEX.find(tranId)?.value?.toLong()
            ?: error("api_tran_id에서 숫자 추출 실패: $tranId")
    }

    private companion object {
        val TRAN_ID_REGEX = Regex(""""api_tran_id":"([^"]+)"""")
        val SEQ_DIGITS_REGEX = Regex("""\d+""")
    }
}
