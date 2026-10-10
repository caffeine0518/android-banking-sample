package com.study.bank.data.remote.kftc.dto.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 거래내역 `res_list`의 한 건. KFTC 통장 거래 표기를 따른다.
 *
 * `inout_type`은 "입금"/"출금", `print_content`는 통장 인자내용(상대방 또는 메모)이다.
 * `tran_seq`는 KFTC 기본 응답에 없는 mock 확장 필드다. 행마다 단조 증가하는 고유값이라 합성 TransactionId가
 * 충돌하지 않는다. 기본값 0은 seq를 쓰지 않는 단위 테스트 픽스처용이다.
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
