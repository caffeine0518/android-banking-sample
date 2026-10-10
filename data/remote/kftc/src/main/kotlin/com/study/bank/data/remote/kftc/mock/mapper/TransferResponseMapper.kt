package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.service.model.WithdrawResult

internal class TransferResponseMapper(private val tranIds: KftcTranIds) {

    /** 재요청도 같은 거래로 식별되도록 bank_tran_id는 요청 값을 그대로 반환한다. */
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

    fun toRejectedResponse(bankRspCode: String, message: String): WithdrawTransferResponse =
        WithdrawTransferResponse(
            apiTranId = tranIds.newApiTranId(),
            apiTranDtm = tranIds.nowDtm(),
            rspCode = RSP_ERROR,
            rspMessage = message,
            bankRspCode = bankRspCode,
        )
}
