package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 출금이체 응답.
 *
 * 성공하면 rsp_code "A0000"과 출금계좌 정보, 출금 후 잔액(after_balance_amt)을 반환한다. 잔액 부족 같은
 * 업무 거절은 KFTC 스펙대로 HTTP 200 + rsp_code "A0001" + bank_rsp_code로 반환하며 계좌 상세 필드가 비어 있을 수
 * 있다. 성공과 거절을 같은 DTO로 역직렬화하므로 상세 필드는 널러블로 둔다.
 */
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
