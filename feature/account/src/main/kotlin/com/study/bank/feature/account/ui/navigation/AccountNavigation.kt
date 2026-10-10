package com.study.bank.feature.account.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.study.bank.feature.account.ui.AccountDetailRoute
import kotlinx.serialization.Serializable

/** 진입과 복귀(백스택 절단) 모두 data class 동등성으로 같은 목적지를 찾는다. */
@Serializable
data class AccountRoute(val accountId: String) : NavKey

fun EntryProviderScope<NavKey>.accountEntry(
    onSendClick: (accountId: String) -> Unit,
    onBack: () -> Unit,
) {
    entry<AccountRoute> { key ->
        AccountDetailRoute(route = key, onSendClick = onSendClick, onBack = onBack)
    }
}
