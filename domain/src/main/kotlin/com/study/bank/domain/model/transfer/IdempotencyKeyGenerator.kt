package com.study.bank.domain.model.transfer

/** 송금 한 건마다 [TransferRequest.idempotencyKey]를 발급한다. */
fun interface IdempotencyKeyGenerator {
    fun newKey(): String
}
