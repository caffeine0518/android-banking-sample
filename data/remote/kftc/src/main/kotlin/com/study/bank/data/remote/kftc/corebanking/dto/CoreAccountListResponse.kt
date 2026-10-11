package com.study.bank.data.remote.kftc.corebanking.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoreAccountListResponse(
    @SerialName("accounts") val accounts: List<CoreAccountDto>,
)
