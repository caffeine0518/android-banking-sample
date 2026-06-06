package com.study.bank.data.remote.kftc.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 `account/list_finuse` 응답의 `res_list` 항목.
 *
 * 실제 KFTC 스펙은 잔액/통화를 분리된 `account/balance/fin_num` 엔드포인트로 내려주므로
 * 이 DTO에는 잔액 필드가 없다. (잔액은 [AccountBalanceResponse] 참조)
 */
@Serializable
data class FintechAccountDto(
    @SerialName("fintech_use_num") val fintechUseNum: String,
    @SerialName("account_alias") val accountAlias: String? = null,
    @SerialName("bank_code_std") val bankCodeStd: String,
    @SerialName("bank_name") val bankName: String,
    @SerialName("account_num_masked") val accountNumMasked: String,
    @SerialName("account_holder_name") val accountHolderName: String,
    @SerialName("account_holder_type") val accountHolderType: String,
    @SerialName("account_type") val accountType: String,
    @SerialName("inquiry_agree_yn") val inquiryAgreeYn: String = "Y",
    @SerialName("transfer_agree_yn") val transferAgreeYn: String = "Y",
)
