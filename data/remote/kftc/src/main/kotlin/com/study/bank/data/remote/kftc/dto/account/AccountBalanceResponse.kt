package com.study.bank.data.remote.kftc.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 `GET /v2.0/account/balance/fin_num` 응답.
 *
 * `currency_code`는 실제 KFTC 잔액조회의 외환 확장 필드. 다통화 도메인 시연을 위해 mock에서 항상 채워준다.
 */
@Serializable
data class AccountBalanceResponse(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
    @SerialName("bank_tran_id") val bankTranId: String,
    @SerialName("bank_code_tran") val bankCodeTran: String,
    @SerialName("bank_rsp_code") val bankRspCode: String,
    @SerialName("fintech_use_num") val fintechUseNum: String,
    @SerialName("balance_amt") val balanceAmt: String,
    @SerialName("available_amt") val availableAmt: String,
    @SerialName("account_type") val accountType: String,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("currency_code") val currencyCode: String,
)
