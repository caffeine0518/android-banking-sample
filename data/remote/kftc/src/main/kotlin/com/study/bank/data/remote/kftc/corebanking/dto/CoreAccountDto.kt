package com.study.bank.data.remote.kftc.corebanking.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 오픈뱅킹과 달리 계정계는 본인 계좌의 전체 계좌번호를 반환한다. */
@Serializable
data class CoreAccountDto(
    @SerialName("fintech_use_num") val fintechUseNum: String,
    @SerialName("account_num") val accountNum: String,
)
