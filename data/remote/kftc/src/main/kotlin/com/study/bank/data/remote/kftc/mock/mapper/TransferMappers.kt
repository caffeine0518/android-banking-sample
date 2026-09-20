package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.service.WithdrawResult

/**
 * [WithdrawResult.Success] → KFTC 출금이체 성공 응답 순수 매퍼.
 *
 * 업무 거절(잔액부족 등)은 같은 DTO를 [withdrawRejected]가 rsp_code A0001로 채운다.
 */
internal fun WithdrawResult.Success.toResponse(envelope: KftcEnvelope): WithdrawTransferResponse =
    WithdrawTransferResponse(
        apiTranId = envelope.apiTranId,
        apiTranDtm = envelope.apiTranDtm,
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = envelope.bankTranId,
        bankTranDate = envelope.bankTranDate,
        bankCodeTran = bankCodeStd,
        bankRspCode = BANK_RSP_OK,
        fintechUseNum = fintechUseNum,
        accountNumMasked = accountNumMasked,
        accountHolderName = accountHolderName,
        tranAmt = tranAmt,
        afterBalanceAmt = afterBalanceAmt,
    )

/** 업무 거절. 성공과 같은 DTO를 쓰되 계좌 상세는 비우고 식별용 [bankRspCode]만 채운다. */
internal fun withdrawRejected(
    envelope: KftcEnvelope,
    bankRspCode: String,
    message: String,
): WithdrawTransferResponse = WithdrawTransferResponse(
    apiTranId = envelope.apiTranId,
    apiTranDtm = envelope.apiTranDtm,
    rspCode = RSP_ERROR,
    rspMessage = message,
    bankRspCode = bankRspCode,
)
