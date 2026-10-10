package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.study.bank.feature.account.ui.navigation.AccountRoute
import com.study.bank.feature.account.ui.navigation.accountEntry
import com.study.bank.feature.home.ui.navigation.HomeRoute
import com.study.bank.feature.home.ui.navigation.homeEntry
import com.study.bank.feature.transfer.navigation.TransferAccountInputRoute
import com.study.bank.feature.transfer.navigation.TransferRecipientRoute
import com.study.bank.feature.transfer.navigation.transferAccountInputEntry
import com.study.bank.feature.transfer.navigation.transferAmountEntry
import com.study.bank.feature.transfer.navigation.transferConfirmEntry
import com.study.bank.feature.transfer.navigation.transferRecipientEntry
import com.study.bank.feature.transfer.navigation.transferResultEntry

@Composable
fun BankNavHost() {
    val backStack = rememberNavBackStack(HomeRoute)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.pop() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider(backStack),
    )
}

@Composable
private fun entryProvider(backStack: NavBackStack<NavKey>): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        homeEntry(
            onAccountClick = { accountId -> backStack.push(AccountRoute(accountId)) },
        )
        accountEntry(
            onSendClick = { accountId -> backStack.push(TransferRecipientRoute(accountId)) },
            onBack = { backStack.pop() },
        )
        transferRecipientEntry(
            onBack = { backStack.pop() },
            onAccountNumberInput = { sourceId ->
                backStack.push(TransferAccountInputRoute(sourceId))
            },
            onAmountInput = { route -> backStack.push(route) },
        )
        transferAccountInputEntry(
            onBack = { backStack.pop() },
            onResolved = { route -> backStack.push(route) },
        )
        transferAmountEntry(
            onBack = { backStack.pop() },
            onNext = { route -> backStack.push(route) },
        )
        transferConfirmEntry(
            onBack = { backStack.pop() },
            onSent = { route -> backStack.push(route) },
        )
        transferResultEntry(
            onFinish = { sourceAccountId ->
                backStack.popUpTo { it is AccountRoute && it.accountId == sourceAccountId }
            },
        )
    }

/**
 * 클릭은 intent→effect→콜백을 거쳐 백스택에 반영되므로, 그 사이 더블탭이 같은 키를 두 번 push할 수 있다.
 * 두 엔트리가 contentKey를 공유해 상태·VM이 섞이므로 최상단과 같은 키는 무시한다.
 */
private fun NavBackStack<NavKey>.push(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

/** 백버튼 더블탭으로 백스택이 비면 NavDisplay에서 크래시가 발생하므로 루트 엔트리는 남긴다. */
private fun NavBackStack<NavKey>.pop() {
    if (size > 1) removeLastOrNull()
}

/** Nav2 popUpTo(inclusive=false) 대응. 라우트에 필드가 늘어도 매칭되도록 앵커를 값이 아니라 식별 필드로 찾는다. */
private inline fun NavBackStack<NavKey>.popUpTo(predicate: (NavKey) -> Boolean) {
    val index = indexOfLast(predicate)
    if (index < 0) return
    while (lastIndex > index) removeAt(lastIndex)
}
