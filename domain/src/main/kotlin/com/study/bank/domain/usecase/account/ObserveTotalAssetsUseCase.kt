package com.study.bank.domain.usecase.account

import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.AssetTotals
import com.study.bank.domain.repository.AccountRepository
import com.study.bank.domain.repository.FxRateRepository
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTotalAssetsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val fxRateRepository: FxRateRepository,
) {

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
