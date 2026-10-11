package com.study.bank.data.remote.kftc.corebanking.api

import com.study.bank.data.remote.kftc.corebanking.dto.CoreAccountListResponse
import retrofit2.http.GET

/** 자행 계정계 API. 오픈뱅킹은 계좌번호를 마스킹해 반환하므로 자행 계좌의 전체 계좌번호는 여기서 조회한다. */
interface CoreBankingApiService {

    @GET("core/v1/accounts")
    suspend fun getAccounts(): CoreAccountListResponse
}
