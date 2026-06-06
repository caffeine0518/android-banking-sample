package com.study.bank.data.remote.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** KFTC 오픈뱅킹 v2.0 `GET /v2.0/account/list_finuse` 응답 본문. */
@Serializable
data class AccountListResponse(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
    @SerialName("user_seq_no") val userSeqNo: String,
    @SerialName("res_cnt") val resCnt: String,
    @SerialName("res_list") val resList: List<FintechAccountDto>,
)
