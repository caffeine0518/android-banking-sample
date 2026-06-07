package com.study.bank.data.repository.fx

import android.util.Log
import com.study.bank.data.remote.fx.api.KeximApiService
import com.study.bank.domain.model.Currency
import com.study.bank.domain.repository.FxRateRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class FxRateRepositoryImpl @Inject constructor(
    private val keximApi: KeximApiService,
    private val mapper: FxRateMapper,
    private val clock: Clock,
) : FxRateRepository {

    override fun observeRates(target: Currency): Flow<Map<Currency, BigDecimal>> = flow {
        emit(fetchRates(target))
    }

    /**
     * KEXIM publishes the day's rates around 11:00 KST and skips weekends/holidays.
     * Walk back up to [MAX_WALKBACK] days from yesterday until a non-empty result=1
     * response is found.
     */
    private suspend fun fetchRates(target: Currency): Map<Currency, BigDecimal> {
        var date = LocalDate.now(clock).minusDays(1)
        repeat(MAX_WALKBACK) {
            val items = runCatching { keximApi.getRates(date) }
                .onFailure { Log.w(TAG, "KEXIM call failed for $date", it) }
                .getOrNull()
                .orEmpty()
            if (items.isNotEmpty() && items.first().result == RESULT_SUCCESS) {
                mapper.map(items, target)?.let { return it }
                Log.w(TAG, "Target $target not derivable from KEXIM response on $date — walkback continues")
            }
            date = date.minusDays(1)
        }
        Log.w(TAG, "No KEXIM rates within $MAX_WALKBACK days — returning identity only")
        return mapOf(target to BigDecimal.ONE)
    }

    private companion object {
        const val TAG = "FxRateRepository"
        const val MAX_WALKBACK = 10
        const val RESULT_SUCCESS = 1
    }
}
