package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult

/** 출금 결과 → KFTC `/v2.0/transfer/…` 응답 DTO. 성공과 업무 거절에 같은 DTO를 쓴다. */
internal class TransferResponseMapper(private val tranIds: KftcTranIds) {

    /** bank_tran_id는 새로 발급하지 않고 요청 값을 그대로 반환한다. 재요청도 같은 거래로 식별돼야 한다. */
    fun toResponse(result: WithdrawResult.Success): WithdrawTransferResponse =
        WithdrawTransferResponse(
            apiTranId = tranIds.newApiTranId(),
            apiTranDtm = tranIds.nowDtm(),
            rspCode = RSP_SUCCESS,
            rspMessage = "",
            bankTranId = result.bankTranId,
            bankTranDate = tranIds.nowDate(),
            bankCodeTran = result.bankCodeStd,
            bankRspCode = BANK_RSP_OK,
            fintechUseNum = result.fintechUseNum,
            accountNumMasked = result.accountNumMasked,
            accountHolderName = result.accountHolderName,
            tranAmt = result.tranAmt,
            afterBalanceAmt = result.afterBalanceAmt,
        )

    /** 업무 거절. 계좌 상세는 비우고 거절 사유를 식별하는 [bankRspCode]만 포함한다. */
    fun toRejectedResponse(bankRspCode: String, message: String): WithdrawTransferResponse =
        WithdrawTransferResponse(
            apiTranId = tranIds.newApiTranId(),
            apiTranDtm = tranIds.nowDtm(),
            rspCode = RSP_ERROR,
            rspMessage = message,
            bankRspCode = bankRspCode,
        )
}
