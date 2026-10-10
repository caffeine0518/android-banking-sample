package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_ACTIVE
import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_INACTIVE
import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.BANK_RSP_RECIPIENT_NOT_FOUND
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.mock.http.response.KftcTranIds
import com.study.bank.data.remote.kftc.mock.model.SeedRecipient

internal class InquiryResponseMapper(private val tranIds: KftcTranIds) {

    fun toResponse(recipient: SeedRecipient): RealNameInquiryResponse = RealNameInquiryResponse(
        apiTranId = tranIds.newApiTranId(),
        apiTranDtm = tranIds.nowDtm(),
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = tranIds.newBankTranId(),
        bankTranDate = tranIds.nowDate(),
        bankCodeTran = recipient.bankCodeStd,
        bankRspCode = BANK_RSP_OK,
        accountNum = recipient.accountNum,
        accountHolderName = recipient.holderName,
        accountId = recipient.accountId,
        accountStatus = if (recipient.active) ACCOUNT_STATUS_ACTIVE else ACCOUNT_STATUS_INACTIVE,
    )

    fun toNotFoundResponse(accountNum: String): RealNameInquiryResponse = RealNameInquiryResponse(
        apiTranId = tranIds.newApiTranId(),
        apiTranDtm = tranIds.nowDtm(),
        rspCode = RSP_ERROR,
        rspMessage = "조회된 예금주가 없습니다",
        bankRspCode = BANK_RSP_RECIPIENT_NOT_FOUND,
        accountNum = accountNum,
    )
}
