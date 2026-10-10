package com.study.bank.data.remote.kftc.dto.inquiry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealNameInquiryRequest(
    @SerialName("bank_tran_id") val bankTranId: String,
    @SerialName("bank_code_std") val bankCodeStd: String,
    @SerialName("account_num") val accountNum: String,
    @SerialName("tran_dtime") val tranDtime: String,
    @SerialName("account_holder_info_type") val accountHolderInfoType: String = "",
    @SerialName("account_holder_info") val accountHolderInfo: String = "",
)
