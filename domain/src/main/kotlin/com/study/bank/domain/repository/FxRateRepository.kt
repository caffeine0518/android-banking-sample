package com.study.bank.domain.repository

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface FxRateRepository {

    /** 각 통화를 [target]으로 환산하는 배율 맵. `target 금액 = 원본 금액 * rate`이고 `target → 1`은 항상 포함된다. */
    fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>>
}
