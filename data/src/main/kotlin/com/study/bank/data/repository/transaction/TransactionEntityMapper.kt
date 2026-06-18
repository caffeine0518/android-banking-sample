package com.study.bank.data.repository.transaction

import com.study.bank.data.local.entity.TransactionEntity
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.Counterparty
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transaction.TransactionType
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionEntityMapper @Inject constructor() {

    fun toEntity(transaction: Transaction): TransactionEntity = TransactionEntity(
        id = transaction.id.value,
        accountId = transaction.accountId.value,
        type = transaction.type.name,
        amount = transaction.amount.amount.toPlainString(),
        currency = transaction.amount.currency.code,
        balanceAfter = transaction.balanceAfter.amount.toPlainString(),
        counterpartyName = transaction.counterparty?.name,
        memo = transaction.memo,
        occurredAt = transaction.occurredAt.toEpochMilli(),
        status = transaction.status.name,
    )

    /**
     * @throws IllegalStateException Entity는 우리가 저장한 값이므로 통화/enum 복원 실패는 스키마-코드 정합성이
     * 깨진 상황. fail-fast.
     */
    fun toDomain(entity: TransactionEntity): Transaction {
        val currency = checkNotNull(Currency.byCode(entity.currency)) {
            "Unsupported currency in DB: ${entity.currency}"
        }
        val type = checkNotNull(TransactionType.entries.firstOrNull { it.name == entity.type }) {
            "Unknown transaction type in DB: ${entity.type}"
        }
        val status = checkNotNull(TransactionStatus.entries.firstOrNull { it.name == entity.status }) {
            "Unknown transaction status in DB: ${entity.status}"
        }
        return Transaction(
            id = TransactionId(entity.id),
            accountId = AccountId(entity.accountId),
            type = type,
            amount = Money.of(BigDecimal(entity.amount), currency),
            balanceAfter = Money.of(BigDecimal(entity.balanceAfter), currency),
            counterparty = entity.counterpartyName?.let { Counterparty(it, null, null) },
            memo = entity.memo,
            occurredAt = Instant.ofEpochMilli(entity.occurredAt),
            status = status,
        )
    }
}
