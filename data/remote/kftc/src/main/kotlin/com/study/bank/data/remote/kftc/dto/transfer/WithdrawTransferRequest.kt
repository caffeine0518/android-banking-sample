package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 출금이체(`transfer/withdraw/fin_num`) 요청 본문.
 *
 * `fintech_use_num`은 출금계좌(이용기관에 등록된 계좌)이고, 수취 계좌는 `recv_client_*`로 식별한다.
 * mock은 (recv_client_account_num, recv_client_bank_code_std)가 시드 계좌와 일치하면 내부 이체로 보고
 * 입금까지 처리한다. KFTC 스펙의 핵심 필드만 포함했다.
 */
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
