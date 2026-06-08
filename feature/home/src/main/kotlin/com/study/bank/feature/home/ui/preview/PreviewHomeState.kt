package com.study.bank.feature.home.ui.preview

import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.model.AccountTypeUi
import com.study.bank.feature.home.ui.model.AccountUi
import java.math.BigDecimal

internal val PreviewHomeState = HomeState(
    accounts = listOf(
        AccountUi(
            id = "acc-1",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "월급통장",
            balance = MoneyUi(BigDecimal("2847320"), CurrencyUi.KRW),
        ),
        AccountUi(
            id = "acc-2",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "외화통장 USD",
            balance = MoneyUi(BigDecimal("3245.80"), CurrencyUi.USD),
        ),
        AccountUi(
            id = "acc-3",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.SAVINGS,
            nickname = "세이프박스",
            balance = MoneyUi(BigDecimal("12000000"), CurrencyUi.KRW),
        ),
        AccountUi(
            id = "acc-4",
            bankDisplayName = "신한은행",
            type = AccountTypeUi.CHECKING,
            nickname = null,
            balance = MoneyUi(BigDecimal("450000"), CurrencyUi.KRW),
        ),
        AccountUi(
            id = "acc-5",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "엔화 여행통장",
            balance = MoneyUi(BigDecimal("128400"), CurrencyUi.JPY),
        ),
        AccountUi(
            id = "acc-6",
            bankDisplayName = "토스뱅크",
            type = AccountTypeUi.CHECKING,
            nickname = "유로 여행통장",
            balance = MoneyUi(BigDecimal("842.15"), CurrencyUi.EUR),
        ),
    ),
    totalAssets = MoneyUi(BigDecimal("19957280"), CurrencyUi.KRW),
)
