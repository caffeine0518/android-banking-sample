package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** mock은 수취 계좌(recv_client_*)가 시드 계좌와 일치하면 내부 이체로 보고 입금까지 처리한다. */
@Serializable
data class WithdrawTransferRequest(
    @SerialName("bank_tran_id") val bankTranId: String,
    @SerialName("fintech_use_num") val fintechUseNum: String,
    @SerialName("tran_amt") val tranAmt: String,
    @SerialName("tran_dtime") val tranDtime: String,
    @SerialName("req_client_name") val reqClientName: String,
    @SerialName("recv_client_name") val recvClientName: String,
    @SerialName("recv_client_bank_code_std") val recvClientBankCodeStd: String,
    @SerialName("recv_client_account_num") val recvClientAccountNum: String,
    @SerialName("wd_print_content") val wdPrintContent: String? = null,
    @SerialName("dps_print_content") val dpsPrintContent: String? = null,
    @SerialName("transfer_purpose") val transferPurpose: String = "TR",
)
