package com.study.bank.data.remote.fx.api

import com.study.bank.data.remote.fx.dto.KeximRateItem
import java.time.LocalDate

interface KeximApiService {

    /** 비영업일·장애 시 빈 리스트나 `result != 1` 항목이 반환될 수 있으므로 호출 측이 result를 확인한다. */
    suspend fun getRates(date: LocalDate): List<KeximRateItem>
}
