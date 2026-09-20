package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.http.response.KftcMockResponses
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.service.WithdrawResult

/**
 * [WithdrawResult.Success] → KFTC 출금이체 성공 응답 순수 매퍼.
 *
 * 업무 거절(잔액부족 등)은 같은 DTO를 [KftcMockResponses.withdrawFailure]가 rsp_code A0001로 직접 조립한다.
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
