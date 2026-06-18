package com.study.bank.data.remote.kftc.mock.dispatcher

import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.mock.SeedRecipient

/**
 * [SeedRecipient] → KFTC 계좌실명조회 성공 응답 순수 매퍼.
 *
 * active 플래그를 ACTIVE/INACTIVE 와이어 상태로 변환한다. 조회 실패(미존재)는
 * [KftcMockResponses.realNameNotFound]가 rsp_code A0001로 직접 조립한다.
 */
internal fun SeedRecipient.toRealNameResponse(
    apiTranId: String,
    apiTranDtm: String,
    bankTranId: String,
    bankTranDate: String,
): RealNameInquiryResponse = RealNameInquiryResponse(
    apiTranId = apiTranId,
    apiTranDtm = apiTranDtm,
    rspCode = RSP_SUCCESS,
    rspMessage = "",
    bankTranId = bankTranId,
    bankTranDate = bankTranDate,
    bankCodeTran = bankCodeStd,
    bankRspCode = BANK_RSP_OK,
    accountNum = accountNum,
    accountHolderName = holderName,
    accountId = accountId,
    accountStatus = if (active) ACCOUNT_STATUS_ACTIVE else ACCOUNT_STATUS_INACTIVE,
)
