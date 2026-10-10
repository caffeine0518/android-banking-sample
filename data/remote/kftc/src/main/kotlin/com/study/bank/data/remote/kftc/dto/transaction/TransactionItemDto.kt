package com.study.bank.data.remote.kftc.dto.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionItemDto(
    @SerialName("tran_date") val tranDate: String,
    @SerialName("tran_time") val tranTime: String,
    @SerialName("inout_type") val inoutType: String,
    @SerialName("tran_type") val tranType: String,
    @SerialName("print_content") val printContent: String,
    @SerialName("tran_amt") val tranAmt: String,
    @SerialName("after_balance_amt") val afterBalanceAmt: String,
    // mock 확장 필드. 행마다 유일한 단조 증가 값이라 합성 TransactionId의 키로 쓴다.
    // 기본값 0은 seq가 필요 없는 테스트 픽스처용이다.
    @SerialName("tran_seq") val tranSeq: Long = 0,
)
