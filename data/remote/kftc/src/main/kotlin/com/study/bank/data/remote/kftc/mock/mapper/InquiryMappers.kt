package com.study.bank.data.remote.kftc.mock.mapper

import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_ACTIVE
import com.study.bank.data.remote.kftc.api.ACCOUNT_STATUS_INACTIVE
import com.study.bank.data.remote.kftc.api.BANK_RSP_OK
import com.study.bank.data.remote.kftc.api.BANK_RSP_RECIPIENT_NOT_FOUND
import com.study.bank.data.remote.kftc.api.RSP_ERROR
import com.study.bank.data.remote.kftc.api.RSP_SUCCESS
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import com.study.bank.data.remote.kftc.mock.seed.SeedRecipient

/**
 * [SeedRecipient] → KFTC 계좌실명조회 성공 응답 순수 매퍼.
 *
 * active 플래그를 ACTIVE/INACTIVE 와이어 상태로 변환한다. 조회 실패는 [realNameNotFound].
 */
internal fun SeedRecipient.toRealNameResponse(envelope: KftcEnvelope): RealNameInquiryResponse =
    RealNameInquiryResponse(
        apiTranId = envelope.apiTranId,
        apiTranDtm = envelope.apiTranDtm,
        rspCode = RSP_SUCCESS,
        rspMessage = "",
        bankTranId = envelope.bankTranId,
        bankTranDate = envelope.bankTranDate,
        bankCodeTran = bankCodeStd,
        bankRspCode = BANK_RSP_OK,
        accountNum = accountNum,
        accountHolderName = holderName,
        accountId = accountId,
        accountStatus = if (active) ACCOUNT_STATUS_ACTIVE else ACCOUNT_STATUS_INACTIVE,
    )

/** 수취 계좌 미존재. KFTC는 업무 거절을 HTTP 200 + rsp_code A0001 + bank_rsp_code로 알린다. */
internal fun realNameNotFound(envelope: KftcEnvelope, accountNum: String): RealNameInquiryResponse =
    RealNameInquiryResponse(
        apiTranId = envelope.apiTranId,
        apiTranDtm = envelope.apiTranDtm,
        rspCode = RSP_ERROR,
        rspMessage = "조회된 예금주가 없습니다",
        bankRspCode = BANK_RSP_RECIPIENT_NOT_FOUND,
        accountNum = accountNum,
    )
