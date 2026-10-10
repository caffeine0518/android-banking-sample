package com.study.bank.data.repository.fx

import com.study.bank.data.remote.fx.api.KeximRates
import com.study.bank.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FxRateMapper @Inject constructor(
    private val rebaser: CurrencyRebaser,
) {

    fun map(rates: KeximRates.Published, target: Currency): Map<Currency, BigDecimal>? {
        val parsed = parseKeximRates(rates)
        if (parsed.isEmpty()) return null
        // KEXIM 응답은 KRW-anchored.
        val anchored = parsed + (Currency.KRW to BigDecimal.ONE)
        if (target !in anchored) return null
        return rebaser.rebase(anchored, target)
    }

    private fun parseKeximRates(rates: KeximRates.Published): Map<Currency, BigDecimal> = buildMap {
        rates.items.forEach { item ->
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
        const val SCALE = 8
    }
}
