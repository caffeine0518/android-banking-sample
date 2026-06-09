package com.study.bank.domain.usecase.account

import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AssetTotals
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class TotalAssetsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val fxRateRepository: FxRateRepository,
) {

    /**
     * 자산을 [target] 기준 합계와 환산 불가 잔액으로 분리해 emit. 환율 시트가 갱신되거나
     * 계좌 변동이 있을 때마다 새로운 [AssetTotals] 방출.
     */
    operator fun invoke(target: Currency): Flow<AssetTotals> =
        accountRepository.observeAccounts().combine(
            fxRateRepository.observeRates(target),
        ) { accounts, rates ->
            val (convertible, missing) = accounts.partition {
                rates.containsKey(it.balance.currency)
            }
            val converted = convertible.fold(Money.zero(target)) { acc, account ->
                val rate = rates.getValue(account.balance.currency)
                val targetAmount = account.balance.amount
                    .multiply(rate)
                    .setScale(target.exponent, RoundingMode.HALF_UP)
                acc + Money.of(targetAmount, target)
            }
            AssetTotals(
                converted = converted,
                unconverted = missing.map { it.balance },
            )
        }
}
