package com.study.bank.data.remote.kftc.mock.http.response

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

private val DTM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

internal class KftcTranIds {
    private val apiTranSeq = AtomicLong(0)
    private val bankTranSeq = AtomicLong(0)

    fun newApiTranId(): String = "T%016d".format(apiTranSeq.incrementAndGet())

    fun newBankTranId(): String = "M202300001U%06d".format(bankTranSeq.incrementAndGet())

    fun nowDtm(): String = LocalDateTime.now().format(DTM_FORMATTER)

    fun nowDate(): String = LocalDateTime.now().format(DATE_FORMATTER)
}
