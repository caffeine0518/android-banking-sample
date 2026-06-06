package com.study.bank.data.remote.api

import com.study.bank.data.remote.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.dto.account.AccountListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 금융결제원 오픈뱅킹 v2.0 일부 엔드포인트의 Retrofit 바인딩.
 *
 * KFTC 스펙대로 계좌 목록과 잔액은 별도 호출이다. Repository 레이어가 목록을 받은 뒤
 * 각 fintech_use_num에 대해 balance/fin_num을 fan-out으로 병렬 호출하는 것을 가정한다.
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
}
