package com.study.bank.domain.usecase.account

import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
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
     * Sum of all account balances converted into [target].
     *
     * Accounts whose currency lacks a known FX rate are excluded — defensive against
     * partial rate sheets so the total still renders.
     */
    operator fun invoke(target: Currency): Flow<Money> =
        accountRepository.observeAccounts().combine(
            fxRateRepository.observeRates(target),
        ) { accounts, rates ->
            accounts.fold(Money.zero(target)) { acc, account ->
                val rate = rates[account.balance.currency] ?: return@fold acc
                val converted = account.balance.amount
                    .multiply(rate)
                    .setScale(target.exponent, RoundingMode.HALF_UP)
                acc + Money.of(converted, target)
            }
        }
}
