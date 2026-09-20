package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.INOUT_DEPOSIT
import com.study.bank.data.remote.kftc.api.INOUT_WITHDRAW
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.api.TRAN_TYPE_TRANSFER
import com.study.bank.data.remote.kftc.dto.transaction.TransactionItemDto
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.storage.SeedAccount
import com.study.bank.data.remote.kftc.mock.storage.TransactionDirection
import com.study.bank.data.remote.kftc.mock.storage.TransactionRecord

/**
 * [TransactionRecord] → KFTC 거래내역 DTO 순수 매퍼.
 *
 * 방향 enum을 와이어 문자열("입금"/"출금")로, tran_type를 "이체"로 고정 변환한다.
 */
internal fun TransactionRecord.toItemDto(): TransactionItemDto = TransactionItemDto(
    tranSeq = seq,
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
    envelope: KftcEnvelope,
    account: SeedAccount,
    nextPageYn: String = "N",
    beforInquiryTraceInfo: String = "",
): TransactionListResponse = TransactionListResponse(
    apiTranId = envelope.apiTranId,
    apiTranDtm = envelope.apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    bankTranId = envelope.bankTranId,
    fintechUseNum = account.fintechUseNum,
    balanceAmt = account.balanceAmt,
    currencyCode = account.currencyCode,
    resCnt = size.toString(),
    resList = map { it.toItemDto() },
    nextPageYn = nextPageYn,
    beforInquiryTraceInfo = beforInquiryTraceInfo,
)
