package com.study.bank.data.repository.transfer

import com.study.bank.data.repository.bankTranIdOf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버는 bank_tran_id 로 멱등 판정을 하므로, 다른 송금이 같은 번호를 받으면 앞 송금의 재시도로 판정된다.
 * 실서비스에서는 은행 백엔드가 발급하지만 이 프로젝트는 KFTC 를 직접 호출하는 mock 이라 앱이 발급한다.
 *
 * ponytail: 발급 기록이 인메모리라 mock 원장(인메모리 Room)과 수명이 같다. 원장을 영속화하면 이 기록도 영속화한다.
 */
@Singleton
class BankTranIdIssuer @Inject constructor() {
    private val issued = ConcurrentHashMap<String, String>()
    private val sequence = AtomicLong()

    fun issue(idempotencyKey: String): String =
        issued.computeIfAbsent(idempotencyKey) { bankTranIdOf(sequence.incrementAndGet()) }
}
