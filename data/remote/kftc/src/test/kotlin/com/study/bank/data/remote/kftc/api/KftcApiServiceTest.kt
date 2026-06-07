package com.study.bank.data.remote.kftc.api

import com.study.bank.data.remote.kftc.mock.KftcMockServer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class KftcApiServiceTest {

    private lateinit var mockServer: KftcMockServer
    private lateinit var api: KftcApiService

    @Before
    fun setUp() {
        mockServer = KftcMockServer().apply { start() }

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                mockServer.clientCertificates.sslSocketFactory(),
                mockServer.clientCertificates.trustManager,
            )
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.baseUrl().toString())
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        api = retrofit.create(KftcApiService::class.java)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `list_finuse 응답에 시드된 4개 계좌가 들어있고 잔액 필드는 없다`() = runTest {
        val response = api.getAccountList(userSeqNo = "1100000001")

        assertEquals("A0000", response.rspCode)
        assertEquals("4", response.resCnt)
        assertEquals(4, response.resList.size)

        val bankCodes = response.resList.map { it.bankCodeStd }
        assertEquals(listOf("092", "092", "092", "088"), bankCodes)

        val aliases = response.resList.map { it.accountAlias }
        assertEquals(listOf("월급통장", "외화통장 USD", "세이프박스", null), aliases)
    }

    @Test
    fun `balance fin_num이 KRW 시드 계좌의 잔액과 통화코드를 정확히 돌려준다`() = runTest {
        val krwFintechUseNum = "120220112345678901234001"

        val balance = api.getAccountBalance(
            bankTranId = "M202300001U000001",
            fintechUseNum = krwFintechUseNum,
            tranDtime = "20260603120000",
        )

        assertEquals("A0000", balance.rspCode)
        assertEquals(krwFintechUseNum, balance.fintechUseNum)
        assertEquals("2847320", balance.balanceAmt)
        assertEquals("KRW", balance.currencyCode)
    }

    @Test
    fun `balance fin_num이 USD 외화통장의 소수점 잔액을 그대로 돌려준다`() = runTest {
        val usdFintechUseNum = "120220112345678901234002"

        val balance = api.getAccountBalance(
            bankTranId = "M202300001U000002",
            fintechUseNum = usdFintechUseNum,
            tranDtime = "20260603120000",
        )

        assertEquals("3245.80", balance.balanceAmt)
        assertEquals("USD", balance.currencyCode)
    }

    @Test
    fun `list_finuse 결과를 순회하며 각 계좌 잔액을 fan-out 조회할 수 있다`() = runTest {
        val list = api.getAccountList(userSeqNo = "1100000001")

        val balances = list.resList.map { item ->
            api.getAccountBalance(
                bankTranId = "M202300001U${"%06d".format(item.fintechUseNum.hashCode() and 0xFFFFF)}",
                fintechUseNum = item.fintechUseNum,
                tranDtime = "20260603120000",
            )
        }

        assertEquals(4, balances.size)
        balances.forEach { assertEquals("A0000", it.rspCode) }

        val pairs = balances.map { it.currencyCode to it.balanceAmt }.toSet()
        assertEquals(
            setOf(
                "KRW" to "2847320",
                "USD" to "3245.80",
                "KRW" to "12000000",
                "KRW" to "450000",
            ),
            pairs,
        )
    }

    @Test
    fun `존재하지 않는 fintech_use_num은 404로 떨어진다`() = runTest {
        val ex = runCatching {
            api.getAccountBalance(
                bankTranId = "M202300001U999999",
                fintechUseNum = "999999999999999999999999",
                tranDtime = "20260603120000",
            )
        }.exceptionOrNull()

        assertNotNull(ex)
        assertTrue(ex is HttpException)
        assertEquals(404, (ex as HttpException).code())
    }

    @Test
    fun `list_finuse 요청 path와 query parameter가 KFTC 스펙대로 전송된다`() = runTest {
        api.getAccountList(userSeqNo = "1100000001")

        val recorded = mockServer.takeRequest()
        assertNotNull(recorded)
        assertEquals("GET", recorded!!.method)

        val path = recorded.path.orEmpty()
        assertTrue("KFTC list_finuse 경로 prefix가 맞아야 한다: $path",
            path.startsWith("/v2.0/account/list_finuse"))
        assertTrue("user_seq_no 쿼리가 전달돼야 한다: $path",
            path.contains("user_seq_no=1100000001"))
        assertTrue("기본 include_cancel_yn=N 쿼리가 함께 가야 한다: $path",
            path.contains("include_cancel_yn=N"))
        assertTrue("기본 sort_order=D 쿼리가 함께 가야 한다: $path",
            path.contains("sort_order=D"))
    }
}
