package com.study.bank.data.remote.kftc.dto.inquiry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 계좌실명조회 응답.
 *
 * 성공하면 rsp_code "A0000"과 예금주명·계좌 식별자·상태를 반환한다. 수취 계좌가 없으면 KFTC 스펙대로
 * HTTP 200 + rsp_code "A0001" + bank_rsp_code를 반환하고 상세 필드가 비어 있으므로 널러블로 둔다.
 * `account_status`는 mock 확장 필드로, 휴면·해지 계좌를 RecipientLookup.Inactive로 구분한다.
 */
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
    @SerialName("account_status") val accountStatus: String? = null,
)
