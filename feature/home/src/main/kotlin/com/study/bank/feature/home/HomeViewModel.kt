package com.study.bank.feature.home

import androidx.lifecycle.viewModelScope
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.Money
import com.study.bank.domain.model.account.Account
import com.study.bank.domain.model.account.AccountId
import com.study.bank.domain.model.account.AccountNumber
import com.study.bank.domain.model.account.AccountType
import com.study.bank.core.ui.mvi.MviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : MviViewModel<HomeState, HomeIntent, HomeEffect>(
    initialState = HomeState(),
) {

    init {
        reduce { copy(accounts = MOCK_ACCOUNTS) }
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> refresh()
            is HomeIntent.AccountClicked ->
                sendEffect(HomeEffect.NavigateToAccountDetail(intent.accountId))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            reduce { copy(isLoading = true) }
            delay(REFRESH_DELAY_MS)
            reduce { copy(accounts = MOCK_ACCOUNTS, isLoading = false) }
        }
    }

    private companion object {
        const val REFRESH_DELAY_MS = 500L

        val MOCK_ACCOUNTS = listOf(
            Account(
                id = AccountId("acc-1"),
                number = AccountNumber("1000123456789"),
                bankCode = BankCode.TOSS,
                holderName = "홍길동",
                balance = Money.of(2_847_320L, Currency.KRW),
                type = AccountType.CHECKING,
                nickname = "월급통장",
            ),
            Account(
                id = AccountId("acc-2"),
                number = AccountNumber("1000987654321"),
                bankCode = BankCode.TOSS,
                holderName = "홍길동",
                balance = Money.of("3245.80", Currency.USD),
                type = AccountType.CHECKING,
                nickname = "외화통장 USD",
            ),
            Account(
                id = AccountId("acc-3"),
                number = AccountNumber("1000555544443"),
                bankCode = BankCode.TOSS,
                holderName = "홍길동",
                balance = Money.of(12_000_000L, Currency.KRW),
                type = AccountType.SAVINGS,
                nickname = "세이프박스",
            ),
            Account(
                id = AccountId("acc-4"),
                number = AccountNumber("110234567890"),
                bankCode = BankCode.SHINHAN,
                holderName = "홍길동",
                balance = Money.of(450_000L, Currency.KRW),
                type = AccountType.CHECKING,
                nickname = null,
            ),
        )
    }
}
