package com.study.bank.data.remote.kftc.mock.http.response

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

private val DTM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * 응답 추적 필드(api_tran_id·bank_tran_id·거래 일시) 생성기.
 *
 * api_tran_id가 엔드포인트와 관계없이 1씩 증가해야 하므로 mock 서버당 인스턴스 하나를 모든 매퍼가 공유한다.
 * 응답 DTO에 없는 필드는 매퍼가 요청하지 않으므로 번호가 소모되지 않는다.
 */
internal class KftcTranIds {
    private val apiTranSeq = AtomicLong(0)
    private val bankTranSeq = AtomicLong(0)

    fun newApiTranId(): String = "T%016d".format(apiTranSeq.incrementAndGet())

    fun newBankTranId(): String = "M202300001U%06d".format(bankTranSeq.incrementAndGet())

    fun nowDtm(): String = LocalDateTime.now().format(DTM_FORMATTER)

    fun nowDate(): String = LocalDateTime.now().format(DATE_FORMATTER)
}
