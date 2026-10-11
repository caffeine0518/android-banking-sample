package com.study.bank.data.remote.kftc.mock.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountNumDigitsTest {

    @Test
    fun `하이픈 유무와 관계없이 숫자만 남긴 값으로 판정한다`() {
        assertEquals("10005511144443", AccountNumDigits.parseOrNull("1000-55-11144443")?.value)
        assertEquals("10005511144443", AccountNumDigits.parseOrNull("10005511144443")?.value)
    }

    @Test
    fun `숫자와 구분용 하이픈 외의 문자가 있으면 계좌번호로 보지 않는다`() {
        listOf("1000-55-***4443", "1000 55 1114443", "1000-55-111a443", "", "-1000-55", "1000--55").forEach {
            assertNull("\"$it\" 는 계좌번호가 아니다", AccountNumDigits.parseOrNull(it))
        }
    }
}
