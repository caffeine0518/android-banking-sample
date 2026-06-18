package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 출금이체 응답.
 *
 * 성공은 rsp_code "A0000" + 출금계좌 정보 + 출금 후 잔액(after_balance_amt). 업무 거절(잔액부족 등)은
 * KFTC대로 HTTP 200 + rsp_code "A0001" + 식별용 bank_rsp_code로 내려오며, 이때 계좌 상세 필드는 비어
 * 있을 수 있어 nullable로 둔다(같은 DTO로 성공/거절을 모두 역직렬화).
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
