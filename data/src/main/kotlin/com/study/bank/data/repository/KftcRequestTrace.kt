package com.study.bank.data.repository

/** 데모 고정값. */
internal const val TRAN_DTIME = "20260603120000"

/**
 * 송금은 서버가 이 값으로 멱등 판정을 하므로 충돌이 곧 결함이다 — 정상 송금이 중복으로 처리된다.
 * 조회 계열은 추적용이라 무관하지만 엄격한 쪽에 맞춘다.
 *
 * ponytail: String.hashCode 32비트가 상한. 무손실이 필요해지면 매핑 테이블로 교체한다.
 */
internal fun bankTranIdFor(seed: String): String =
    "M202300001U%09d".format((seed.hashCode().toLong() and 0xFFFFFFFFL) % 1_000_000_000L)
