package com.study.bank.data.repository.recipient

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transfer.RecipientLookup
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipientMapperTest {

    private val mapper = RecipientMapper()

    @Test
    fun `성공+ACTIVE는 Active로 매핑된다`() {
        val result = mapper.map(response(status = "ACTIVE", holder = "김토스", accountId = "ext-1"))

        assertEquals(RecipientLookup.Active(AccountId("ext-1"), "김토스"), result)
    }

    @Test
    fun `성공+INACTIVE는 Inactive로 매핑된다`() {
        val result = mapper.map(response(status = "INACTIVE", holder = "이휴면", accountId = "ext-2"))

        assertEquals(RecipientLookup.Inactive(AccountId("ext-2"), "이휴면"), result)
    }

    @Test
    fun `rsp_code가 성공이 아니면 NotFound`() {
        assertEquals(
            RecipientLookup.NotFound,
            mapper.map(response(rsp = "A0001", holder = null, accountId = null, status = null)),
        )
    }

    @Test
    fun `성공이어도 예금주명이 없으면 NotFound`() {
        assertEquals(RecipientLookup.NotFound, mapper.map(response(holder = null)))
    }

    @Test
    fun `성공이어도 식별자가 없으면 NotFound`() {
        assertEquals(RecipientLookup.NotFound, mapper.map(response(accountId = null)))
    }

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
}
