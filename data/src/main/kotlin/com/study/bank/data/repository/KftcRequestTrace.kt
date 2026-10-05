package com.study.bank.data.repository

import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** KFTC 일시 필드(tran_dtime, tran_date+tran_time)의 형식. */
internal val KFTC_DATETIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
internal val KST: ZoneId = ZoneId.of("Asia/Seoul")

/** KFTC 요청의 tran_dtime. 기기 시간대와 무관하게 KST 로 적는다. */
internal fun tranDtime(clock: Clock): String = KFTC_DATETIME_FORMAT.format(clock.instant().atZone(KST))

/**
 * 송금은 서버가 이 값으로 멱등 판정을 하므로 충돌이 곧 결함이다 — 정상 송금이 중복으로 처리된다.
 * 조회 계열은 추적용이라 무관하지만 엄격한 쪽에 맞춘다.
 *
 * ponytail: String.hashCode 32비트가 상한. 무손실이 필요해지면 매핑 테이블로 교체한다.
 */
internal fun bankTranIdFor(seed: String): String =
    "M202300001U%09d".format((seed.hashCode().toLong() and 0xFFFFFFFFL) % 1_000_000_000L)
