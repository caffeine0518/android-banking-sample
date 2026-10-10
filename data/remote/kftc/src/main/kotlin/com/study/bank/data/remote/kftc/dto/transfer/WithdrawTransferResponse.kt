package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 업무 거절은 HTTP 200 + rsp_code A0001로 계좌 상세 없이 반환되므로, 같은 DTO로 받도록 상세 필드는 널러블이다. */
@Serializable
data class WithdrawTransferResponse(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
    @SerialName("bank_tran_id") val bankTranId: String? = null,
    @SerialName("bank_tran_date") val bankTranDate: String? = null,
    @SerialName("bank_code_tran") val bankCodeTran: String? = null,
    @SerialName("bank_rsp_code") val bankRspCode: String? = null,
    @SerialName("fintech_use_num") val fintechUseNum: String? = null,
    @SerialName("account_num_masked") val accountNumMasked: String? = null,
    @SerialName("account_holder_name") val accountHolderName: String? = null,
    @SerialName("tran_amt") val tranAmt: String? = null,
    @SerialName("after_balance_amt") val afterBalanceAmt: String? = null,
)
