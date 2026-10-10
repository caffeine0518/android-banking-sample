package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.api.KftcApiService
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.repository.NoopKftcApiService
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.transfer.RecipientLookup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RecipientRepositoryImplTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-06-18T01:30:00Z"), ZoneOffset.UTC)

    @Test
    fun `활성 수취인은 Active로 조회된다`() = runTest {
        val api = FakeKftcApiService(response(status = "ACTIVE", holder = "김토스", accountId = "ext-1"))

        val result = buildRepo(api).lookup(AccountNumber("110-555-667788"), BankCode.SHINHAN)

        assertEquals(RecipientLookup.Active(AccountId("ext-1"), "김토스"), result)
    }

    @Test
    fun `휴면 수취인은 Inactive로 조회된다`() = runTest {
        val api = FakeKftcApiService(response(status = "INACTIVE", holder = "이휴면", accountId = "ext-2"))

        val result = buildRepo(api).lookup(AccountNumber("004-999-888777"), BankCode.KB)

        assertEquals(RecipientLookup.Inactive(AccountId("ext-2"), "이휴면"), result)
    }

    @Test
    fun `미존재 계좌는 NotFound로 조회된다`() = runTest {
        val api = FakeKftcApiService(response(rsp = "A0001", holder = null, accountId = null, status = null))

        val result = buildRepo(api).lookup(AccountNumber("0000-00-0000000"), BankCode.TOSS)

        assertEquals(RecipientLookup.NotFound, result)
    }

    @Test
    fun `요청에 수취 은행코드와 계좌번호가 실린다`() = runTest {
        val api = FakeKftcApiService(response())

        buildRepo(api).lookup(AccountNumber("110-555-667788"), BankCode.SHINHAN)

        assertEquals("088", api.lastRequest?.bankCodeStd)
        assertEquals("110-555-667788", api.lastRequest?.accountNum)
    }

    @Test
    fun `요청의 거래일시는 주입된 Clock 의 현재 시각을 KST 로 적는다`() = runTest {
        val api = FakeKftcApiService(response())

        buildRepo(api).lookup(AccountNumber("110-555-667788"), BankCode.SHINHAN)

        // fixedClock 2026-06-18T01:30:00Z 를 KST 로 환산한 값
        assertEquals("20260618103000", api.lastRequest?.tranDtime)
    }

    private fun buildRepo(api: KftcApiService) = RecipientRepositoryImpl(api, RecipientMapper(), fixedClock)

    private fun response(
        rsp: String = "A0000",
        holder: String? = "홍길동",
        accountId: String? = "acc-1",
        status: String? = "ACTIVE",
    ) = RealNameInquiryResponse(
        apiTranId = "T0000000000000001",
        apiTranDtm = "20260618103000000",
        rspCode = rsp,
        rspMessage = "",
        accountHolderName = holder,
        accountId = accountId,
        accountStatus = status,
    )

    private class FakeKftcApiService(
        private val response: RealNameInquiryResponse,
    ) : KftcApiService by NoopKftcApiService {
        var lastRequest: RealNameInquiryRequest? = null
            private set

        override suspend fun inquireRealName(request: RealNameInquiryRequest): RealNameInquiryResponse {
            lastRequest = request
            return response
        }
    }
}
