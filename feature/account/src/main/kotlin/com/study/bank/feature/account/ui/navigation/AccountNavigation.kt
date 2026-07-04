package com.study.bank.feature.account.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.account.ui.AccountDetailRoute
import kotlinx.serialization.Serializable

/**
 * 계좌 상세 화면 내비 키. [accountId]=fintech_use_num. 진입(add)과 복귀(백스택 절단) 양쪽에서
 * 데이터 클래스 동등성으로 백스택의 동일 목적지를 식별한다.
 */
@Serializable
data class AccountRoute(val accountId: String) : NavKey

fun accountRoute(accountId: AccountId) = AccountRoute(accountId.value)

fun EntryProviderScope<NavKey>.accountEntry(
    onSendClick: (AccountId) -> Unit,
    onBack: () -> Unit,
) {
    entry<AccountRoute> { key ->
        AccountDetailRoute(route = key, onSendClick = onSendClick, onBack = onBack)
    }
}
