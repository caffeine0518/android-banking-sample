package com.study.bank.data.remote.kftc.api

import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.AccountListResponse
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryRequest
import com.study.bank.data.remote.kftc.dto.inquiry.RealNameInquiryResponse
import com.study.bank.data.remote.kftc.dto.transaction.TransactionListResponse
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferRequest
import com.study.bank.data.remote.kftc.dto.transfer.WithdrawTransferResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 금융결제원 오픈뱅킹 v2.0 엔드포인트 일부의 Retrofit 인터페이스.
 *
 * KFTC 스펙에서 계좌 목록과 잔액은 별도 API다. Repository가 목록을 조회한 뒤 계좌마다
 * `balance/fin_num`을 병렬로 호출하는 것을 전제로 한다.
 */
interface KftcApiService {

    @GET("v2.0/account/list_finuse")
    suspend fun getAccountList(
        @Query("user_seq_no") userSeqNo: String,
        @Query("include_cancel_yn") includeCancelYn: String = "N",
        @Query("sort_order") sortOrder: String = "D",
    ): AccountListResponse

    @GET("v2.0/account/balance/fin_num")
    suspend fun getAccountBalance(
        @Query("bank_tran_id") bankTranId: String,
        @Query("fintech_use_num") fintechUseNum: String,
        @Query("tran_dtime") tranDtime: String,
    ): AccountBalanceResponse

    @GET("v2.0/account/transaction_list/fin_num")
    suspend fun getTransactionList(
        @Query("bank_tran_id") bankTranId: String,
        @Query("fintech_use_num") fintechUseNum: String,
        @Query("from_date") fromDate: String,
        @Query("to_date") toDate: String,
        @Query("tran_dtime") tranDtime: String,
        @Query("inquiry_type") inquiryType: String = "A",
        @Query("inquiry_base") inquiryBase: String = "D",
        @Query("sort_order") sortOrder: String = "D",
        @Query("befor_inquiry_trace_info") beforInquiryTraceInfo: String? = null,
    ): TransactionListResponse

    @POST("v2.0/transfer/withdraw/fin_num")
    suspend fun withdraw(@Body request: WithdrawTransferRequest): WithdrawTransferResponse

    @POST("v2.0/inquiry/real_name")
    suspend fun inquireRealName(@Body request: RealNameInquiryRequest): RealNameInquiryResponse
}
