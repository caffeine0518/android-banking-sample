package com.study.bank.data.remote.kftc.mock.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 오류 응답 본문. 성공 응답과 달리 클라이언트가 역직렬화하지 않으므로 kftc/dto에 대응하는 타입이 없다.
 */
@Serializable
internal data class ErrorEnvelope(
    @SerialName("api_tran_id") val apiTranId: String,
    @SerialName("api_tran_dtm") val apiTranDtm: String,
    @SerialName("rsp_code") val rspCode: String,
    @SerialName("rsp_message") val rspMessage: String,
)
