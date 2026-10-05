package com.study.bank.data.repository.transfer

import com.study.bank.data.repository.bankTranIdOf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 송금의 멱등성 키마다 bank_tran_id 를 순번으로 한 번만 발급한다. 같은 키는 항상 같은 번호를, 다른 키는 항상
 * 다른 번호를 받는다 — 서버는 번호로 멱등 판정을 하므로, 다른 송금이 같은 번호를 받으면 앞 송금의 재시도로 판정된다.
 *
 * 실제로는 앱이 멱등성 키만 은행 백엔드에 보내고 bank_tran_id 는 백엔드가 발급한다. 이 프로젝트는 KFTC 를
 * 직접 호출하는 mock 이라 이 클래스가 그 역할을 대신한다.
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
