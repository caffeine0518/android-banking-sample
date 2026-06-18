package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.WithdrawResult

/**
 * [WithdrawResult.Success] → KFTC 출금이체 성공 응답 순수 매퍼.
 *
 * 업무 거절(잔액부족 등)은 같은 DTO를 [KftcMockResponses.withdrawFailure]가 rsp_code A0001로 직접 조립한다.
 * envelope/tran_id/date는 호출 측이 채운다.
 */
internal fun WithdrawResult.Success.toResponse(
    apiTranId: String,
    apiTranDtm: String,
    bankTranId: String,
    bankTranDate: String,
): WithdrawTransferResponse = WithdrawTransferResponse(
    apiTranId = apiTranId,
    apiTranDtm = apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    bankTranId = bankTranId,
    bankTranDate = bankTranDate,
    bankCodeTran = bankCodeStd,
    bankRspCode = BANK_RSP_OK,
    fintechUseNum = fintechUseNum,
    accountNumMasked = accountNumMasked,
    accountHolderName = accountHolderName,
    tranAmt = tranAmt,
    afterBalanceAmt = afterBalanceAmt,
)
