package com.study.bank.data.remote.kftc.dto.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KFTC 오픈뱅킹 v2.0 거래내역조회(`transaction_list/fin_num`) 응답.
 *
 * envelope + 조회 계좌의 현재 잔액 + 거래 목록. `res_list`는 sort_order=D(최신순) 기준.
 */
@Serializable
data class TransactionListResponse(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
    @SerialName("bank_tran_id") val bankTranId: String,
    @SerialName("fintech_use_num") val fintechUseNum: String,
    @SerialName("balance_amt") val balanceAmt: String,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("res_cnt") val resCnt: String,
    @SerialName("res_list") val resList: List<TransactionItemDto>,
    @SerialName("next_page_yn") val nextPageYn: String = "N",
    @SerialName("befor_inquiry_trace_info") val beforInquiryTraceInfo: String = "",
)
