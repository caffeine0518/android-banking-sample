package com.study.bank.domain.repository

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface FxRateRepository {

    /**
     * 다른 통화를 [target]으로 환산하는 환율 스트림.
     *
     * 발행되는 맵은 원본 [Currency]를 배율에 대응시킨다:
     * `target.amount = source.amount * rate`. 항등 항목인
     * `target → 1`은 항상 포함된다.
     */
    fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>>
}
