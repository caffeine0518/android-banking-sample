package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import java.time.LocalDate

/**
 * KEXIM 환율 조회 모듈의 공개 진입점.
 *
 * 호출 측은 [LocalDate]만 넘기면 됨 — 인증키 주입, 날짜 포맷팅, HTTP 디테일은 구현체가 처리.
 */
interface KeximApiService {

    /**
     * 지정된 날짜의 KEXIM 환율 시세 전체 (전 통화).
     *
     * 비영업일/장애 시 빈 리스트 또는 `result != 1` 항목이 섞여 올 수 있음 — 호출 측이 result 검사.
     */
    suspend fun getRates(date: LocalDate): List<KeximRateItem>
}
