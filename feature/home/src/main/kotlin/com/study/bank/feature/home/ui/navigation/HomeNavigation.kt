package com.study.bank.feature.home.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.home.ui.HomeRoute as HomeRouteScreen
import kotlinx.serialization.Serializable

/** 홈(계좌 목록) 화면 내비 키. 백스택의 루트 목적지. */
@Serializable
data object HomeRoute : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onAccountClick: (AccountId) -> Unit,
) {
    entry<HomeRoute> {
        HomeRouteScreen(onAccountClick = onAccountClick)
    }
}
