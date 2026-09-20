package com.study.bank.data.remote.kftc.mock.model

/**
 * 응답마다 새로 발급하는 추적 필드 묶음.
 *
 * 매퍼가 이 값들을 낱개 String으로 받으면 전부 같은 타입이라 순서가 바뀌어도 컴파일된다. 값만 넘겨
 * 매퍼가 시퀀스·시계 생성기에 의존하지 않게 하는 목적도 겸한다. 대응 필드가 없는 응답(계좌목록에는
 * bank_tran_id가 없다)은 해당 값을 쓰지 않는다.
 */
internal data class KftcEnvelope(
    val apiTranId: String,
    val apiTranDtm: String,
    val bankTranId: String,
    val bankTranDate: String,
)
