package com.study.bank.data.repository.transaction

import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.transaction.Counterparty
import com.study.bank.domain.model.transaction.Transaction
import com.study.bank.domain.model.transaction.TransactionId
import com.study.bank.domain.model.transaction.TransactionStatus
import com.study.bank.domain.model.transaction.TransactionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * KFTC 거래내역 `res_list` 항목 → 도메인 [Transaction] 매퍼.
 *
 * KFTC 기본 거래내역엔 행 고유 id가 없어 (계좌+발생일시+조회순번)으로 합성한다. 통화는 응답 계좌 단위라
 * 호출 측이 [currency]로 넘긴다. inout_type/tran_type 한글 라벨은 KFTC 와이어 계약값이며 mock은 전부
 * 완료 거래라 status는 COMPLETED로 둔다.
 */
@Singleton
class TransactionMapper @Inject constructor() {

    fun map(
        dto: TransactionItemDto,
        accountId: AccountId,
        currency: Currency,
        index: Int,
    ): Transaction = Transaction(
        id = TransactionId("${accountId.value}-${dto.tranDate}${dto.tranTime}-$index"),
        accountId = accountId,
        type = resolveType(dto.inoutType, dto.tranType),
        amount = Money.of(dto.tranAmt, currency),
        balanceAfter = Money.of(dto.afterBalanceAmt, currency),
        counterparty = dto.printContent
            .takeIf(String::isNotBlank)
            ?.let { Counterparty(name = it, accountNumber = null, bankCode = null) },
        memo = null,
        occurredAt = parseOccurredAt(dto.tranDate, dto.tranTime),
        status = TransactionStatus.COMPLETED,
    )

    private fun resolveType(inoutType: String, tranType: String): TransactionType {
        val transfer = tranType == TRAN_TYPE_TRANSFER
        return if (inoutType == INOUT_DEPOSIT) {
            if (transfer) TransactionType.TRANSFER_IN else TransactionType.DEPOSIT
        } else {
            if (transfer) TransactionType.TRANSFER_OUT else TransactionType.WITHDRAWAL
        }
    }

    private fun parseOccurredAt(tranDate: String, tranTime: String) =
        LocalDateTime.parse(tranDate + tranTime, OCCURRED_AT_FORMAT).atZone(KST).toInstant()

    private companion object {
        // KFTC 와이어 계약값(:data:remote:kftc의 KftcProtocol과 동일 문자열).
        const val INOUT_DEPOSIT = "입금"
        const val TRAN_TYPE_TRANSFER = "이체"
        val KST: ZoneId = ZoneId.of("Asia/Seoul")
        val OCCURRED_AT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}
