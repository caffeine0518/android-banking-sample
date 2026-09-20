package com.study.bank.data.remote.kftc.mock.http.response

import com.study.bank.data.remote.kftc.mock.model.KftcEnvelope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

private val DTM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

/**
 * [KftcEnvelope] 발급기.
 *
 * api_tran_id 시퀀스가 엔드포인트 전역으로 1씩 증가해야 하므로 mock 서버 하나당 인스턴스 하나를
 * 모든 핸들러가 공유한다.
 */
internal class KftcEnvelopes {
    private val apiTranSeq = AtomicLong(0)
    private val bankTranSeq = AtomicLong(0)

    /** [bankTranId]를 주면 새로 발급하지 않는다 — 출금 재요청은 같은 거래여야 한다. */
    fun next(bankTranId: String = newBankTranId()): KftcEnvelope {
        val now = LocalDateTime.now()
        return KftcEnvelope(
            apiTranId = "T%016d".format(apiTranSeq.incrementAndGet()),
            apiTranDtm = now.format(DTM_FORMATTER),
            bankTranId = bankTranId,
            bankTranDate = now.format(DATE_FORMATTER),
        )
    }

    private fun newBankTranId(): String = "M202300001U%06d".format(bankTranSeq.incrementAndGet())
}
