package com.study.bank.domain.model.transfer

import com.study.bank.domain.model.account.AccountId
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipientLookupTest {

    private val accountId = AccountId("acc-1")

    @Test
    fun `Active carries account id and holder name`() {
        val lookup: RecipientLookup = RecipientLookup.Active(accountId, "홍길동")
        assertEquals(RecipientLookup.Active(accountId, "홍길동"), lookup)
    }

    @Test
    fun `Inactive carries account id and holder name`() {
        val lookup: RecipientLookup = RecipientLookup.Inactive(accountId, "홍길동")
        assertEquals(RecipientLookup.Inactive(accountId, "홍길동"), lookup)
    }

    @Test
    fun `NotFound is a singleton`() {
        val lookup: RecipientLookup = RecipientLookup.NotFound
        assertEquals(RecipientLookup.NotFound, lookup)
    }
}
