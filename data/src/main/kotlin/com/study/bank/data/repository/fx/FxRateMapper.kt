package com.study.bank.data.repository.fx

import com.study.bank.data.remote.fx.dto.KeximRateItem
import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FxRateMapper @Inject constructor(
    private val rebaser: CurrencyRebaser,
) {

    /**
     * KEXIM 응답을 target 기준 환율 맵으로 변환. 파싱 가능한 통화가 없거나 target을 KEXIM 데이터에서
     * 유도할 수 없으면 null — 호출자가 "환산 불가" 상태를 명시적으로 처리해야 함.
     */
    fun map(items: List<KeximRateItem>, target: Currency): Map<Currency, BigDecimal>? {
        val parsed = parseKeximRates(items)
        if (parsed.isEmpty()) return null
        // KEXIM 응답은 KRW-anchored.
        val anchored = parsed + (Currency.KRW to BigDecimal.ONE)
        if (target !in anchored) return null
        return rebaser.rebase(anchored, target)
    }

    private fun parseKeximRates(items: List<KeximRateItem>): Map<Currency, BigDecimal> = buildMap {
        items.asSequence()
            .filter { it.result == RESULT_SUCCESS }
            .forEach { item ->
                val (currency, divisor) = parseCurUnit(item.curUnit) ?: return@forEach
                val rate = parseRate(item.dealBasR) ?: return@forEach
                put(currency, rate.divide(divisor, SCALE, RoundingMode.HALF_UP))
            }
    }

    private fun parseCurUnit(curUnit: String?): Pair<Currency, BigDecimal>? = when (curUnit) {
        "USD" -> Currency.USD to BigDecimal.ONE
        "EUR" -> Currency.EUR to BigDecimal.ONE
        "JPY(100)" -> Currency.JPY to BigDecimal(100)
        else -> null
    }

    private fun parseRate(raw: String?): BigDecimal? =
        raw?.replace(",", "")?.toBigDecimalOrNull()

    private companion object {
        const val RESULT_SUCCESS = 1
        const val SCALE = 8
    }
}
