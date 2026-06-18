package com.study.bank.data.remote.kftc.api

import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.network.NetworkJson
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
        mockServer = KftcMockServer(NetworkJson()).apply { start() }

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
    fun `list_finuse 응답에 시드된 6개 계좌가 들어있고 잔액 필드는 없다`() = runTest {
        val response = api.getAccountList(userSeqNo = "1100000001")

        assertEquals("A0000", response.rspCode)
        assertEquals("6", response.resCnt)
        assertEquals(6, response.resList.size)

        val bankCodes = response.resList.map { it.bankCodeStd }
        assertEquals(listOf("092", "092", "092", "088", "092", "092"), bankCodes)

        val aliases = response.resList.map { it.accountAlias }
        assertEquals(
            listOf("월급통장", "외화통장 USD", "세이프박스", null, "대만 여행자금", "베트남 동"),
            aliases,
        )
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

        assertEquals(6, balances.size)
        balances.forEach { assertEquals("A0000", it.rspCode) }

        val pairs = balances.map { it.currencyCode to it.balanceAmt }.toSet()
        assertEquals(
            setOf(
                "KRW" to "2847320",
                "USD" to "3245.80",
                "KRW" to "12000000",
                "KRW" to "450000",
                "TWD" to "12500.50",
                "VND" to "1850000",
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

    // --- 거래내역 조회 / 출금이체 E2E ---

    @Test
    fun `transaction_list는 초기엔 빈 res_list와 현재 잔액을 돌려준다`() = runTest {
        val response = api.getTransactionList(
            bankTranId = "M202300001U000010",
            fintechUseNum = SALARY,
            fromDate = "20260601",
            toDate = "20260618",
            tranDtime = "20260618103000",
        )

        assertEquals("A0000", response.rspCode)
        assertEquals("0", response.resCnt)
        assertTrue(response.resList.isEmpty())
        assertEquals("2847320", response.balanceAmt)
    }

    @Test
    fun `withdraw 성공 후 getAccountBalance가 차감 잔액을 반영한다`() = runTest {
        val result = api.withdraw(externalRequest(from = SALARY, amount = "50000"))

        assertEquals("A0000", result.rspCode)
        assertEquals("2797320", result.afterBalanceAmt)

        val balance = api.getAccountBalance(
            bankTranId = "M202300001U000011",
            fintechUseNum = SALARY,
            tranDtime = "20260618103000",
        )
        assertEquals("2797320", balance.balanceAmt)
    }

    @Test
    fun `내부 이체 시 수취계좌 잔액과 거래내역이 반영된다`() = runTest {
        api.withdraw(internalRequest(from = SALARY, toAccountNum = SAFEBOX_NUM, amount = "50000"))

        val balance = api.getAccountBalance(
            bankTranId = "M202300001U000012",
            fintechUseNum = SAFEBOX,
            tranDtime = "20260618103000",
        )
        assertEquals("12050000", balance.balanceAmt)

        val tx = api.getTransactionList(
            bankTranId = "M202300001U000013",
            fintechUseNum = SAFEBOX,
            fromDate = "20260601",
            toDate = "20260618",
            tranDtime = "20260618103000",
        )
        assertEquals("1", tx.resCnt)
        assertEquals("입금", tx.resList.first().inoutType)
        assertEquals("50000", tx.resList.first().tranAmt)
        assertEquals("12050000", tx.resList.first().afterBalanceAmt)
    }

    @Test
    fun `잔액부족이면 rsp_code A0001과 bank_rsp_code를 돌려준다`() = runTest {
        val result = api.withdraw(externalRequest(from = SALARY, amount = "999999999"))

        assertEquals("A0001", result.rspCode)
        assertEquals("311", result.bankRspCode)
    }

    @Test
    fun `withdraw 요청 body가 KFTC 필드명대로 전송된다`() = runTest {
        api.withdraw(externalRequest(from = SALARY, amount = "50000"))

        val recorded = mockServer.takeRequest()
        assertNotNull(recorded)
        assertEquals("POST", recorded!!.method)
        assertTrue("withdraw 경로: ${recorded.path}",
            recorded.path.orEmpty().startsWith("/v2.0/transfer/withdraw/fin_num"))

        val body = recorded.body.readUtf8()
        assertTrue("fintech_use_num 필드: $body", body.contains("fintech_use_num"))
        assertTrue("recv_client_account_num 필드: $body", body.contains("recv_client_account_num"))
        assertTrue("tran_amt 필드: $body", body.contains("tran_amt"))
    }

    private fun externalRequest(from: String, amount: String) = WithdrawTransferRequest(
        bankTranId = "M202300001U000001",
        fintechUseNum = from,
        tranAmt = amount,
        tranDtime = "20260618103000",
        reqClientName = "홍길동",
        recvClientName = "외부수취",
        recvClientBankCodeStd = "004",
        recvClientAccountNum = "9999-99-9999999",
    )

    private fun internalRequest(from: String, toAccountNum: String, amount: String) = WithdrawTransferRequest(
        bankTranId = "M202300001U000002",
        fintechUseNum = from,
        tranAmt = amount,
        tranDtime = "20260618103000",
        reqClientName = "홍길동",
        recvClientName = "홍길동",
        recvClientBankCodeStd = "092",
        recvClientAccountNum = toAccountNum,
        wdPrintContent = "세이프박스로",
        dpsPrintContent = "월급통장에서",
    )

    private companion object {
        const val SALARY = "120220112345678901234001" // 월급통장 KRW 2847320
        const val SAFEBOX = "120220112345678901234003" // 세이프박스 KRW 12000000
        const val SAFEBOX_NUM = "1000-55-1114443"
    }
}
