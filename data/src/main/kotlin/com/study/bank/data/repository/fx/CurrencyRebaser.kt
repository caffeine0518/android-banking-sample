package com.study.bank.data.repository.fx

import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRebaser @Inject constructor() {

    /**
     * anchor 맵을 target 기준으로 재계산. [target]이 [anchored]에 없으면 환산 자체가 불가능하므로
     * 호출 측이 사전에 보장해야 함. 위반 시 [IllegalArgumentException].
     */
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
