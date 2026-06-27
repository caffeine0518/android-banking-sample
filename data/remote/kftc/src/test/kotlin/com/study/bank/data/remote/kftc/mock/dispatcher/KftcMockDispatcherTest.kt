package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.mock.KftcAccountSeed
import com.study.bank.data.remote.kftc.mock.KftcBankState
import com.study.bank.data.remote.kftc.mock.KftcRecipientSeed
import com.study.bank.data.remote.kftc.mock.KftcSeedAccountIds
import com.study.bank.data.remote.kftc.mock.SeedAccount
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [KftcMockDispatcher] 라우팅/에러 분기 단독 검증.
 *
 * Retrofit/DTO 직렬화 경로는 [com.study.bank.data.remote.kftc.api.KftcApiServiceTest]에서 다루므로
 * 여기선 raw HTTP body 문자열만 보고 dispatcher 책임(경로 매칭, 시드 조회, [MockError] 분기)에 집중한다.
 */
class KftcMockDispatcherTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply {
            dispatcher = newDispatcher()
            start()
        }
        client = OkHttpClient()
    }

    // 협력자를 명시 주입해 디스패처를 조립한다(구성 책임은 호출 측). responses는 단일 인스턴스 공유.
    private fun newDispatcher(seed: List<SeedAccount> = KftcAccountSeed.accounts): KftcMockDispatcher {
        val state = KftcBankState(seed)
        val responses = KftcMockResponses()
        return KftcMockDispatcher(
            accountHandler = AccountRequestHandler(state, responses),
            transferHandler = TransferRequestHandler(state, responses, MOCK_JSON),
            inquiryHandler = InquiryRequestHandler(KftcRecipientSeed.directory(seed), responses, MOCK_JSON),
            responses = responses,
        )
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
        server.dispatcher = newDispatcher(
            seed = listOf(
                SeedAccount(
                    fintechUseNum = "TESTFINNUM0000001",
                    bankCodeStd = "092",
                    bankName = "토스뱅크",
                    accountNum = "100-000-001",
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

    // --- transaction_list / withdraw 라우팅 ---

    @Test
    fun `transaction_list는 200과 envelope + 시드 없는 계좌는 빈 res_list를 돌려준다`() {
        // 월급통장은 시드 히스토리가 있으므로, 라우팅+빈 결과 검증은 시드 없는 신한 계좌로 한다.
        val (code, body) = get("/v2.0/account/transaction_list/fin_num?fintech_use_num=$SHINHAN")

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("거래내역 0건: $body", body.contains(""""res_cnt":"0""""))
        assertTrue("빈 res_list: $body", body.contains(""""res_list":[]"""))
    }

    @Test
    fun `transaction_list fintech_use_num 누락은 400`() {
        val (code, body) = get("/v2.0/account/transaction_list/fin_num")

        assertEquals(400, code)
        assertTrue("MissingFintechUseNum 메시지: $body", body.contains("fintech_use_num 쿼리 누락"))
    }

    @Test
    fun `transaction_list 미존재 fintech_use_num은 404`() {
        val bogus = "999999999999999999999999"

        val (code, body) = get("/v2.0/account/transaction_list/fin_num?fintech_use_num=$bogus")

        assertEquals(404, code)
        assertTrue("입력값 echo: $body", body.contains(bogus))
    }

    @Test
    fun `withdraw 성공 후 balance fin_num이 차감 잔액을 반영한다`() {
        val (code, body) = post("/v2.0/transfer/withdraw/fin_num", externalWithdrawBody(from = SALARY, amount = "50000"))

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")

        val (_, balance) = get("/v2.0/account/balance/fin_num?fintech_use_num=$SALARY")
        assertTrue("출금 후 잔액 2797320 반영: $balance", balance.contains(""""balance_amt":"2797320""""))
    }

    @Test
    fun `내부 이체 후 수취계좌 transaction_list에 입금이 기록되고 잔액이 는다`() {
        post(
            "/v2.0/transfer/withdraw/fin_num",
            internalWithdrawBody(from = SALARY, toAccountNum = SAFEBOX_NUM, amount = "50000"),
        )

        val (_, tx) = get("/v2.0/account/transaction_list/fin_num?fintech_use_num=$SAFEBOX")
        assertTrue("수취계좌 거래 1건: $tx", tx.contains(""""res_cnt":"1""""))
        assertTrue("입금 기록: $tx", tx.contains(""""inout_type":"입금""""))

        val (_, balance) = get("/v2.0/account/balance/fin_num?fintech_use_num=$SAFEBOX")
        assertTrue("세이프박스 잔액 12050000: $balance", balance.contains(""""balance_amt":"12050000""""))
    }

    @Test
    fun `withdraw 잔액부족은 200과 A0001 + bank_rsp_code`() {
        val (code, body) = post("/v2.0/transfer/withdraw/fin_num", externalWithdrawBody(from = SALARY, amount = "999999999"))

        assertEquals(200, code)
        assertTrue("업무 거절 rsp_code A0001: $body", body.contains(""""rsp_code":"A0001""""))
        assertTrue("잔액부족 bank_rsp_code 311: $body", body.contains(""""bank_rsp_code":"311""""))
    }

    @Test
    fun `withdraw 본문 누락은 400 MissingTransferBody`() {
        val (code, body) = post("/v2.0/transfer/withdraw/fin_num", "")

        assertEquals(400, code)
        assertTrue("MissingTransferBody 메시지: $body", body.contains("출금이체 요청 본문"))
    }

    // --- inquiry/real_name 라우팅 ---

    @Test
    fun `real_name 활성 수취인은 200과 예금주명 + ACTIVE를 돌려준다`() {
        val (code, body) = post("/v2.0/inquiry/real_name", realNameBody(bank = "088", accountNum = "110-555-667788"))

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("예금주명: $body", body.contains("김토스"))
        assertTrue("활성 상태: $body", body.contains(""""account_status":"ACTIVE""""))
    }

    @Test
    fun `real_name은 계좌목록의 본인 계좌도 활성 수취인으로 조회한다`() {
        // 계좌 시드에서 파생되므로, 예전 하드코딩 목록에 없던 신한 계좌로도 송금(내부이체) 가능해야 한다.
        val (code, body) = post("/v2.0/inquiry/real_name", realNameBody(bank = "088", accountNum = "110-23-1237890"))

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("본인 예금주: $body", body.contains("홍길동"))
        assertTrue("활성 상태: $body", body.contains(""""account_status":"ACTIVE""""))
        assertTrue("식별자=해당 계좌 fintech_use_num: $body",
            body.contains(""""account_id":"${KftcSeedAccountIds.SHINHAN_KRW}""""))
    }

    @Test
    fun `real_name은 하이픈 없는 숫자 계좌번호도 시드와 매칭한다`() {
        // 앱 입력은 숫자만 받으므로(토스 동일), 하이픈 없는 번호가 하이픈 시드와 매칭돼야 한다.
        val (code, body) = post("/v2.0/inquiry/real_name", realNameBody(bank = "088", accountNum = "110555667788"))

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("예금주명: $body", body.contains("김토스"))
        assertTrue("활성 상태: $body", body.contains(""""account_status":"ACTIVE""""))
    }

    @Test
    fun `real_name 휴면 수취인은 INACTIVE 상태를 돌려준다`() {
        val (code, body) = post("/v2.0/inquiry/real_name", realNameBody(bank = "004", accountNum = "004-999-888777"))

        assertEquals(200, code)
        assertEnvelope(body, rspCode = "A0000")
        assertTrue("휴면 상태: $body", body.contains(""""account_status":"INACTIVE""""))
        assertTrue(body.contains("이휴면"))
    }

    @Test
    fun `real_name 미존재 계좌는 200과 A0001 + bank_rsp_code`() {
        val (code, body) = post("/v2.0/inquiry/real_name", realNameBody(bank = "092", accountNum = "0000-00-0000000"))

        assertEquals(200, code)
        assertTrue("업무 거절 A0001: $body", body.contains(""""rsp_code":"A0001""""))
        assertTrue("수취 없음 bank_rsp_code 012: $body", body.contains(""""bank_rsp_code":"012""""))
    }

    @Test
    fun `real_name 본문 누락은 400 MissingInquiryBody`() {
        val (code, body) = post("/v2.0/inquiry/real_name", "")

        assertEquals(400, code)
        assertTrue("MissingInquiryBody 메시지: $body", body.contains("계좌실명조회 요청 본문"))
    }

    private fun get(path: String): Pair<Int, String> {
        val response = client.newCall(
            Request.Builder().url(server.url(path)).build(),
        ).execute()
        return response.use { it.code to (it.body?.string().orEmpty()) }
    }

    private fun post(path: String, body: String): Pair<Int, String> {
        val response = client.newCall(
            Request.Builder()
                .url(server.url(path))
                .post(body.toRequestBody("application/json".toMediaType()))
                .build(),
        ).execute()
        return response.use { it.code to (it.body?.string().orEmpty()) }
    }

    private fun externalWithdrawBody(from: String, amount: String): String =
        """{"bank_tran_id":"M202300001U000001","fintech_use_num":"$from","tran_amt":"$amount",""" +
            """"tran_dtime":"20260618103000","req_client_name":"홍길동","recv_client_name":"외부수취",""" +
            """"recv_client_bank_code_std":"004","recv_client_account_num":"9999-99-9999999"}"""

    private fun internalWithdrawBody(from: String, toAccountNum: String, amount: String): String =
        """{"bank_tran_id":"M202300001U000002","fintech_use_num":"$from","tran_amt":"$amount",""" +
            """"tran_dtime":"20260618103000","req_client_name":"홍길동","recv_client_name":"홍길동",""" +
            """"recv_client_bank_code_std":"092","recv_client_account_num":"$toAccountNum",""" +
            """"wd_print_content":"세이프박스로","dps_print_content":"월급통장에서"}"""

    private fun realNameBody(bank: String, accountNum: String): String =
        """{"bank_tran_id":"M202300001U000099","bank_code_std":"$bank",""" +
            """"account_num":"$accountNum","tran_dtime":"20260618103000"}"""

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
        val MOCK_JSON = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val TRAN_ID_REGEX = Regex(""""api_tran_id":"([^"]+)"""")
        val SEQ_DIGITS_REGEX = Regex("""\d+""")

        const val SALARY = KftcSeedAccountIds.PAYROLL_KRW
        const val SAFEBOX = KftcSeedAccountIds.SAFEBOX_KRW
        const val SHINHAN = KftcSeedAccountIds.SHINHAN_KRW
        const val SAFEBOX_NUM = "1000-55-1114443"
    }
}
