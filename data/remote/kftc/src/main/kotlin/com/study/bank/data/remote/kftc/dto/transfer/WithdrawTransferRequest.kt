package com.study.bank.data.remote.kftc.dto.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 출금이체(`transfer/withdraw/fin_num`) 요청 본문.
 *
 * `fintech_use_num`은 출금계좌(이용기관 등록 계좌), 수취는 `recv_client_*`로 식별한다.
 * 본 mock은 (recv_client_account_num, recv_client_bank_code_std)가 시드 계좌와 매칭되면
 * 내부 이체로 입금까지 시뮬레이션한다. KFTC 스펙의 핵심 필드만 추렸다.
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
