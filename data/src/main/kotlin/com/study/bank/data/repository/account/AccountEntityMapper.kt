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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountEntityMapper @Inject constructor() {

    fun toEntity(account: Account): AccountEntity = AccountEntity(
        id = account.id.value,
        number = account.number.value,
        bankCode = account.bankCode.code,
        holderName = account.holderName,
        balanceAmount = account.balance.amount.toPlainString(),
        balanceCurrency = account.balance.currency.code,
        type = account.type.name,
        nickname = account.nickname,
    )

    /**
     * @throws IllegalStateException 저장된 enum·code를 복원하지 못하면 발생한다(스키마와 코드 불일치).
     */
    fun toDomain(entity: AccountEntity): Account {
        val bank = checkNotNull(BankCode.byCode(entity.bankCode)) {
            "Unknown bank code in DB: ${entity.bankCode}"
        }
        val currency = checkNotNull(Currency.byCode(entity.balanceCurrency)) {
            "Unsupported currency in DB: ${entity.balanceCurrency}"
        }
        val type = checkNotNull(AccountType.entries.firstOrNull { it.name == entity.type }) {
            "Unknown account type in DB: ${entity.type}"
        }
        return Account(
            id = AccountId(entity.id),
            number = AccountNumber(entity.number),
            bankCode = bank,
            holderName = entity.holderName,
            balance = Money.of(BigDecimal(entity.balanceAmount), currency),
            type = type,
            nickname = entity.nickname,
        )
    }
}
