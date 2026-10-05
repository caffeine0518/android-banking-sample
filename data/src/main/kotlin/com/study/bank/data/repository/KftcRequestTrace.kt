package com.study.bank.data.repository

import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** KFTC 일시 필드(tran_dtime, tran_date+tran_time)의 형식. */
internal val KFTC_DATETIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
internal val KST: ZoneId = ZoneId.of("Asia/Seoul")

/** KFTC 요청의 tran_dtime. 기기 시간대와 무관하게 KST 로 적는다. */
internal fun tranDtime(clock: Clock): String = KFTC_DATETIME_FORMAT.format(clock.instant().atZone(KST))

/** bank_tran_id = 이용기관코드 + "U" + 이용기관 부여번호 9자리. */
internal fun bankTranIdOf(sequence: Long): String = "M202300001U%09d".format(sequence)

/**
 * 조회 요청의 bank_tran_id. 조회는 추적용이라 충돌해도 무관해 [seed] 의 해시로 만든다.
 * 송금은 서버가 이 값으로 멱등 판정을 하므로 [com.study.bank.data.repository.transfer.BankTranIdIssuer] 가 순번으로 발급한다.
 */
internal fun bankTranIdFor(seed: String): String =
    bankTranIdOf((seed.hashCode().toLong() and 0xFFFFFFFFL) % 1_000_000_000L)
