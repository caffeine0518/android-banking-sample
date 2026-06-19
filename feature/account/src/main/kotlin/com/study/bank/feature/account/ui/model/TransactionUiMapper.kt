package com.study.bank.feature.account.ui.model

import com.study.bank.core.ui.mapper.MoneyUiMapper
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionUiMapper @Inject constructor(
    private val moneyUiMapper: MoneyUiMapper,
) {

    fun map(transaction: Transaction): TransactionUi = TransactionUi(
        id = transaction.id.value,
        type = mapType(transaction.type),
        counterpartyName = transaction.counterparty?.name,
        amount = moneyUiMapper.map(transaction.amount),
        occurredAtLabel = DATE_FORMAT.format(transaction.occurredAt),
    )

    private fun mapType(type: TransactionType): TransactionTypeUi = when (type) {
        TransactionType.DEPOSIT -> TransactionTypeUi.DEPOSIT
        TransactionType.WITHDRAWAL -> TransactionTypeUi.WITHDRAWAL
        TransactionType.TRANSFER_IN -> TransactionTypeUi.TRANSFER_IN
        TransactionType.TRANSFER_OUT -> TransactionTypeUi.TRANSFER_OUT
    }

    private companion object {
        // 거래 발생 시각(Instant)을 KST 기준 날짜로 표시.
        val DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.of("Asia/Seoul"))
    }
}
