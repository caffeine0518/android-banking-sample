package com.study.bank.data.remote.kftc.dto.inquiry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 수취 계좌가 없으면 HTTP 200 + rsp_code A0001로 상세 필드 없이 반환되므로 상세 필드는 널러블이다. */
@Serializable
data class RealNameInquiryResponse(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
    @SerialName("bank_tran_id") val bankTranId: String? = null,
    @SerialName("bank_tran_date") val bankTranDate: String? = null,
    @SerialName("bank_code_tran") val bankCodeTran: String? = null,
    @SerialName("bank_rsp_code") val bankRspCode: String? = null,
    @SerialName("account_num") val accountNum: String? = null,
    @SerialName("account_holder_name") val accountHolderName: String? = null,
    @SerialName("account_id") val accountId: String? = null,
    // 휴면·해지 계좌를 구분하는 mock 확장 필드.
    @SerialName("account_status") val accountStatus: String? = null,
)
