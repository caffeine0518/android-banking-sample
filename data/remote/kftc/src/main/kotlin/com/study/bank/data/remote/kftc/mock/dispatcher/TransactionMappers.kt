package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.mock.SeedAccount
import com.study.bank.data.remote.kftc.mock.TransactionDirection
import com.study.bank.data.remote.kftc.mock.TransactionRecord

/**
 * [TransactionRecord] → KFTC 거래내역 DTO 순수 매퍼.
 *
 * 방향 enum을 와이어 문자열("입금"/"출금")로, tran_type를 "이체"로 고정 변환한다.
 * envelope 추적 필드는 호출 측([KftcMockResponses])이 채운다.
 */
internal fun TransactionRecord.toItemDto(): TransactionItemDto = TransactionItemDto(
    tranDate = tranDate,
    tranTime = tranTime,
    inoutType = when (direction) {
        TransactionDirection.DEPOSIT -> INOUT_DEPOSIT
        TransactionDirection.WITHDRAWAL -> INOUT_WITHDRAW
    },
    tranType = TRAN_TYPE_TRANSFER,
    printContent = printContent,
    tranAmt = tranAmt,
    afterBalanceAmt = afterBalanceAmt,
)

internal fun List<TransactionRecord>.toTransactionListResponse(
    account: SeedAccount,
    apiTranId: String,
    apiTranDtm: String,
    bankTranId: String,
): TransactionListResponse = TransactionListResponse(
    apiTranId = apiTranId,
    apiTranDtm = apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    bankTranId = bankTranId,
    fintechUseNum = account.fintechUseNum,
    balanceAmt = account.balanceAmt,
    resCnt = size.toString(),
    resList = map { it.toItemDto() },
)
