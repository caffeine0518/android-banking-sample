package com.study.bank.domain.repository

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface FxRateRepository {

    /**
     * Stream of FX rates converting other currencies into [target].
     *
     * Each emitted map keys a source [Currency] to a multiplier:
     * `target.amount = source.amount * rate`. The identity row
     * `target → 1` is always present.
     */
    fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>>
}
