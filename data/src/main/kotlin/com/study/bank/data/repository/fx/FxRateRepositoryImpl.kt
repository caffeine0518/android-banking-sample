package com.study.bank.data.repository.fx

import android.util.Log
import com.study.bank.data.remote.fx.api.KeximApiService
import com.study.bank.data.remote.fx.api.KeximRates
import com.study.bank.domain.coroutine.cancellableCatching
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
     * KEXIM은 당일 환율을 KST 11:00 무렵 공시하고 주말·공휴일에는 공시하지 않는다.
     * 그래서 어제부터 최대 [MAX_WALKBACK]일까지 거슬러 올라가며 게시된 환율이 나올 때까지 조회한다.
     * 인증키 오류·한도 초과는 날짜를 바꿔도 같으므로 바로 중단한다.
     */
    private suspend fun fetchRates(target: Currency): Map<Currency, BigDecimal> {
        var date = LocalDate.now(clock).minusDays(1)
        repeat(MAX_WALKBACK) {
            val rates = cancellableCatching { keximApi.getRates(date) }
                .onFailure { Log.w(TAG, "KEXIM call failed for $date", it) }
                .getOrDefault(KeximRates.NotPublished)
            when (rates) {
                is KeximRates.Published -> {
                    mapper.map(rates, target)?.let { return it }
                    Log.w(TAG, "Target $target not derivable from KEXIM response on $date — walkback continues")
                }
                KeximRates.NotPublished -> Unit
                KeximRates.InvalidKey, KeximRates.LimitExceeded -> {
                    Log.w(TAG, "KEXIM rejected the request ($rates) — walkback stops")
                    return identity(target)
                }
            }
            date = date.minusDays(1)
        }
        Log.w(TAG, "No KEXIM rates within $MAX_WALKBACK days — returning identity only")
        return identity(target)
    }

    private fun identity(target: Currency): Map<Currency, BigDecimal> = mapOf(target to BigDecimal.ONE)

    private companion object {
        const val TAG = "FxRateRepository"
        const val MAX_WALKBACK = 10
    }
}
