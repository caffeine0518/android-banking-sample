package com.study.bank.feature.home.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.study.bank.feature.home.ui.HomeRoute as HomeRouteScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onAccountClick: (accountId: String) -> Unit,
) {
    entry<HomeRoute> {
        HomeRouteScreen(onAccountClick = onAccountClick)
    }
}
