package com.study.bank.data.remote.kftc.dto.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 거래내역 `res_list` 한 줄. KFTC 통장거래 표기를 따른다.
 *
 * `inout_type`은 "입금"/"출금", `print_content`는 통장 인자내용(상대방/메모)이다.
 * `tran_seq`는 KFTC 기본 응답엔 없는 **mock 확장** — 행마다 단조 증가하는 고유 식별자로, 합성 TransactionId의
 * 충돌 없는 근거가 된다(기본값 0은 seq를 안 쓰는 단위테스트 픽스처용).
 */
@Serializable
data class TransactionItemDto(
    @SerialName("tran_date") val tranDate: String,
    @SerialName("tran_time") val tranTime: String,
    @SerialName("inout_type") val inoutType: String,
    @SerialName("tran_type") val tranType: String,
    @SerialName("print_content") val printContent: String,
    @SerialName("tran_amt") val tranAmt: String,
    @SerialName("after_balance_amt") val afterBalanceAmt: String,
    @SerialName("tran_seq") val tranSeq: Long = 0,
)
