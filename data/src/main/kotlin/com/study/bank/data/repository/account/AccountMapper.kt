package com.study.bank.data.repository.account

import com.study.bank.data.remote.kftc.dto.account.AccountBalanceResponse
import com.study.bank.data.remote.kftc.dto.account.FintechAccountDto
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountMapper @Inject constructor() {

    /**
     * @throws IllegalArgumentException if currency code is unsupported or bank code is unknown
     */
    fun map(dto: FintechAccountDto, balance: AccountBalanceResponse): Account {
        val currency = requireNotNull(Currency.byCode(balance.currencyCode)) {
            "Unsupported currency: ${balance.currencyCode}"
        }
        val bank = requireNotNull(BankCode.byCode(dto.bankCodeStd)) {
            "Unknown bank code: ${dto.bankCodeStd}"
        }
        return Account(
            id = AccountId(dto.fintechUseNum),
            number = AccountNumber(dto.accountNumMasked),
            bankCode = bank,
            holderName = dto.accountHolderName,
            balance = Money.of(balance.balanceAmt, currency),
            type = toAccountType(dto.accountType),
            nickname = dto.accountAlias,
        )
    }

    private fun toAccountType(code: String): AccountType = when (code) {
        KFTC_TYPE_CHECKING -> AccountType.CHECKING
        KFTC_TYPE_SAVINGS -> AccountType.SAVINGS
        KFTC_TYPE_DEPOSIT -> AccountType.DEPOSIT
        else -> AccountType.CHECKING
    }

    private companion object {
        const val KFTC_TYPE_CHECKING = "1"
        const val KFTC_TYPE_SAVINGS = "2"
        const val KFTC_TYPE_DEPOSIT = "3"
    }
}
