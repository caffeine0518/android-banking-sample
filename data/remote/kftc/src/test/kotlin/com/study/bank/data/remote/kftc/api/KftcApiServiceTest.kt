package com.study.bank.data.remote.kftc.api

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.data.remote.kftc.mock.KftcSeedAccountIds
import com.study.bank.data.remote.kftc.mock.KftcTransactionSeed
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
    fun `list_finuse 응답에 시드된 7개 계좌가 들어있고 잔액 필드는 없다`() = runTest {
        val response = api.getAccountList(userSeqNo = "1100000001")

        assertEquals("A0000", response.rspCode)
        assertEquals("7", response.resCnt)
        assertEquals(7, response.resList.size)

        val bankCodes = response.resList.map { it.bankCodeStd }
        assertEquals(listOf("092", "092", "092", "088", "092", "092", "092"), bankCodes)

        val aliases = response.resList.map { it.accountAlias }
        assertEquals(
            listOf("월급통장", "외화통장 USD", "세이프박스", null, "대만 여행자금", "베트남 동", "외화통장 USD 2"),
            aliases,
        )
    }

    @Test
    fun `balance fin_num이 KRW 시드 계좌의 잔액과 통화코드를 정확히 돌려준다`() = runTest {
        val krwFintechUseNum = KftcSeedAccountIds.PAYROLL_KRW

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
        val usdFintechUseNum = KftcSeedAccountIds.FX_USD

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

        assertEquals(7, balances.size)
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
                "USD" to "5000.00",
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
    fun `시드 거래내역이 없는 계좌는 빈 res_list와 next_page_yn=N을 돌려준다`() = runTest {
        // 신한 계좌는 시드 히스토리가 없다(세션 이체도 안 함) → 첫 페이지가 곧 빈 결과.
        val response = api.getTransactionList(
            bankTranId = "M202300001U000010",
            fintechUseNum = SHINHAN,
            fromDate = "20260101",
            toDate = "20261231",
            tranDtime = "20260618103000",
        )

        assertEquals("A0000", response.rspCode)
        assertEquals("0", response.resCnt)
        assertTrue(response.resList.isEmpty())
        assertEquals("N", response.nextPageYn)
        assertEquals("450000", response.balanceAmt)
    }

    @Test
    fun `월급통장 첫 페이지는 서버 페이지 크기만큼 주고 next_page_yn=Y와 다음 커서를 준다`() = runTest {
        val first = api.getTransactionList(
            bankTranId = "M202300001U000020",
            fintechUseNum = SALARY,
            fromDate = "20260101",
            toDate = "20261231",
            tranDtime = "20260618103000",
            // 첫 페이지는 커서 미전송(null).
        )

        assertEquals("A0000", first.rspCode)
        assertEquals(KFTC_TRANSACTION_PAGE_SIZE, first.resList.size)
        assertEquals("Y", first.nextPageYn)
        assertTrue("다음 커서가 있어야 한다", first.beforInquiryTraceInfo.isNotEmpty())
        // 명세서 최신순 → 첫 거래의 잔액은 현재 잔액.
        assertEquals("2847320", first.resList.first().afterBalanceAmt)
    }

    @Test
    fun `커서로 연속조회하면 끝까지 시드 전체를 겹침 없이 받고 마지막은 next_page_yn=N`() = runTest {
        var cursor: String? = null
        var pageCount = 0
        val collected = mutableListOf<com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto>()

        do {
            val page = api.getTransactionList(
                bankTranId = "M202300001U%06d".format(pageCount),
                fintechUseNum = SALARY,
                fromDate = "20260101",
                toDate = "20261231",
                tranDtime = "20260618103000",
                beforInquiryTraceInfo = cursor,
            )
            collected += page.resList
            cursor = if (page.nextPageYn == "Y") page.beforInquiryTraceInfo else null
            pageCount++
        } while (cursor != null && pageCount < 1_000)

        // 전체 시드 건수를 빠짐없이, 중복 없이 받았다.
        assertEquals(KftcTransactionSeed.HISTORY_COUNT, collected.size)
        assertEquals(KftcTransactionSeed.HISTORY_COUNT, collected.distinct().size)
        // ceil(전체 / 페이지) — 비배수여도 맞도록(현재 1200/20=60이지만 가정에 의존하지 않게).
        val expectedPages = (KftcTransactionSeed.HISTORY_COUNT + KFTC_TRANSACTION_PAGE_SIZE - 1) / KFTC_TRANSACTION_PAGE_SIZE
        assertEquals(expectedPages, pageCount)
    }

    @Test
    fun `연속조회 도중 새 거래가 명세서 머리에 끼어도 페이지 경계가 밀리지 않는다`() = runTest {
        // 키셋 커서 회귀 보호: 오프셋 커서였다면 새 거래가 모든 오프셋을 +1 밀어 경계행이 1·2페이지에 중복된다.
        val first = api.getTransactionList(
            bankTranId = "M202300001U000030",
            fintechUseNum = SALARY,
            fromDate = "20260101",
            toDate = "20261231",
            tranDtime = "20260618103000",
        )

        // 페이지 도중 월급통장에 새 이체 발생 → ledger.add(0, …)로 명세서 맨 앞에 끼어든다.
        api.withdraw(externalRequest(from = SALARY, amount = "1000"))

        val second = api.getTransactionList(
            bankTranId = "M202300001U000031",
            fintechUseNum = SALARY,
            fromDate = "20260101",
            toDate = "20261231",
            tranDtime = "20260618103000",
            beforInquiryTraceInfo = first.beforInquiryTraceInfo,
        )

        val firstKeys = first.resList.map { it.tranDate + it.tranTime }.toSet()
        val secondKeys = second.resList.map { it.tranDate + it.tranTime }
        assertEquals(KFTC_TRANSACTION_PAGE_SIZE, second.resList.size)
        assertTrue("1·2페이지가 겹치면 안 된다", secondKeys.none { it in firstKeys })
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

    // --- 계좌실명조회 E2E ---

    @Test
    fun `inquireRealName 활성 수취인은 예금주명과 ACTIVE를 돌려준다`() = runTest {
        val response = api.inquireRealName(realNameRequest(bank = "088", accountNum = "110-555-667788"))

        assertEquals("A0000", response.rspCode)
        assertEquals("김토스", response.accountHolderName)
        assertEquals("ACTIVE", response.accountStatus)
    }

    @Test
    fun `inquireRealName 휴면 수취인은 INACTIVE를 돌려준다`() = runTest {
        val response = api.inquireRealName(realNameRequest(bank = "004", accountNum = "004-999-888777"))

        assertEquals("A0000", response.rspCode)
        assertEquals("INACTIVE", response.accountStatus)
    }

    @Test
    fun `inquireRealName 미존재 계좌는 A0001과 bank_rsp_code를 돌려준다`() = runTest {
        val response = api.inquireRealName(realNameRequest(bank = "092", accountNum = "0000-00-0000000"))

        assertEquals("A0001", response.rspCode)
        assertEquals("012", response.bankRspCode)
    }

    private fun realNameRequest(bank: String, accountNum: String) = RealNameInquiryRequest(
        bankTranId = "M202300001U000099",
        bankCodeStd = bank,
        accountNum = accountNum,
        tranDtime = "20260618103000",
    )

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
        const val SALARY = KftcSeedAccountIds.PAYROLL_KRW
        const val SAFEBOX = KftcSeedAccountIds.SAFEBOX_KRW
        const val SHINHAN = KftcSeedAccountIds.SHINHAN_KRW
        const val SAFEBOX_NUM = "1000-55-1114443"
    }
}
