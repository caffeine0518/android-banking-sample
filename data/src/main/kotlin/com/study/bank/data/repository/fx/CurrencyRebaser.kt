package com.study.bank.data.repository.fx

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRebaser @Inject constructor() {

    fun rebase(
        anchored: Map<Currency, BigDecimal>,
        target: Currency,
    ): Map<Currency, BigDecimal> {
        require(target in anchored) { "target $target missing from anchored map" }
        val targetRate = anchored.getValue(target)
        return anchored.mapValues { (_, anchorPerSource) ->
            anchorPerSource.divide(targetRate, SCALE, RoundingMode.HALF_UP)
        }
    }

    private companion object {
        const val SCALE = 8
    }
}
