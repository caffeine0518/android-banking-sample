package com.study.bank.domain.model.transfer

/** 송금 한 건에 묶이는 멱등성 키([TransferRequest.idempotencyKey])를 발급한다. 구현은 인프라(data-di)가 제공한다. */
fun interface IdempotencyKeyGenerator {
    fun newKey(): String
}
