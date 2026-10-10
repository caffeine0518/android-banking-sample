package com.study.bank.data.remote.kftc.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
