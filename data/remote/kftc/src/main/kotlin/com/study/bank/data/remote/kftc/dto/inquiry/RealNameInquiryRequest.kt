package com.study.bank.data.remote.kftc.dto.inquiry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 계좌실명조회(`inquiry/real_name`) 요청 본문.
 *
 * 송금 전 수취인 검증용. mock은 (bank_code_std, account_num)으로 수취 디렉터리를 조회한다.
 */
@Serializable
data class RealNameInquiryRequest(
    @SerialName("bank_tran_id") val bankTranId: String,
    @SerialName("bank_code_std") val bankCodeStd: String,
    @SerialName("account_num") val accountNum: String,
    @SerialName("tran_dtime") val tranDtime: String,
    @SerialName("account_holder_info_type") val accountHolderInfoType: String = "",
    @SerialName("account_holder_info") val accountHolderInfo: String = "",
)
