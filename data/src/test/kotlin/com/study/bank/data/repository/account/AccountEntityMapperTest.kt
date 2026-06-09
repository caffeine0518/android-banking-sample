package com.study.bank.data.repository.account

import com.study.bank.data.local.entity.AccountEntity
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AccountEntityMapperTest {

    private val mapper = AccountEntityMapper()

    @Test
    fun `도메인 → 엔티티 변환 시 enum과 value class는 원시값으로 평탄화된다`() {
        val account = Account(
            id = AccountId("FINTECH-001"),
            number = AccountNumber("1000-12-***6789"),
            bankCode = BankCode.TOSS,
            holderName = "홍길동",
            balance = Money.of(BigDecimal("2847320"), Currency.KRW),
            type = AccountType.CHECKING,
            nickname = "월급통장",
        )

        val entity = mapper.toEntity(account)

        // primary: enum/value class가 평탄화돼 SQLite primitive로 들어가는지
        assertEquals("FINTECH-001", entity.id)
        assertEquals("1000-12-***6789", entity.number)
        assertEquals("092", entity.bankCode) // TOSS.code
        assertEquals("KRW", entity.balanceCurrency)
        assertEquals("CHECKING", entity.type)
        // secondary: nullable nickname 보존
        assertEquals("월급통장", entity.nickname)
    }

    @Test
    fun `엔티티 → 도메인 라운드트립 시 모든 필드가 보존된다`() {
        val original = Account(
            id = AccountId("FINTECH-002"),
            number = AccountNumber("1000-98-***4321"),
            bankCode = BankCode.TOSS,
            holderName = "김외환",
            balance = Money.of(BigDecimal("3245.80"), Currency.USD),
            type = AccountType.SAVINGS,
            nickname = null,
        )

        val restored = mapper.toDomain(mapper.toEntity(original))

        // BigDecimal은 scale 보존이 핵심 — USD는 exponent=2라 3245.80
        assertEquals(original.id, restored.id)
        assertEquals(original.number, restored.number)
        assertEquals(original.bankCode, restored.bankCode)
        assertEquals(original.holderName, restored.holderName)
        assertEquals(original.balance, restored.balance)
        assertEquals(original.type, restored.type)
        assertNull(restored.nickname)
    }

    @Test
    fun `엔티티에 알 수 없는 bank code가 들어있으면 fail-fast`() {
        val corrupt = sampleEntity().copy(bankCode = "999")

        assertThrows(IllegalStateException::class.java) {
            mapper.toDomain(corrupt)
        }
    }

    @Test
    fun `엔티티에 미지원 통화가 들어있으면 fail-fast`() {
        val corrupt = sampleEntity().copy(balanceCurrency = "XYZ")

        assertThrows(IllegalStateException::class.java) {
            mapper.toDomain(corrupt)
        }
    }

    private fun sampleEntity() = AccountEntity(
        id = "X",
        number = "1000-00-***0000",
        bankCode = "092",
        holderName = "테스트",
        balanceAmount = "0",
        balanceCurrency = "KRW",
        type = "CHECKING",
        nickname = null,
    )
}
