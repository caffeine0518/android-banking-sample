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
import com.study.bank.feature.account.ui.navigation.accountRoute
import com.study.bank.feature.home.ui.navigation.HomeRoute
import com.study.bank.feature.home.ui.navigation.homeEntry
import com.study.bank.feature.transfer.navigation.TransferAccountInputRoute
import com.study.bank.feature.transfer.navigation.transferAccountInputEntry
import com.study.bank.feature.transfer.navigation.transferAmountEntry
import com.study.bank.feature.transfer.navigation.transferConfirmEntry
import com.study.bank.feature.transfer.navigation.transferRecipientEntry
import com.study.bank.feature.transfer.navigation.transferRecipientRoute
import com.study.bank.feature.transfer.navigation.transferResultEntry

@Composable
fun BankNavHost() {
    // Nav3: 백스택을 앱이 소유하는 상태 리스트로 직접 다룬다. rememberNavBackStack이 NavKey 직렬화로
    // 구성변경·프로세스 death를 넘겨 복원한다.
    val backStack = rememberNavBackStack(HomeRoute)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            // 엔트리별 rememberSaveable 상태 유지(기본 데코레이터) + 엔트리별 ViewModelStore 스코핑.
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
            onAccountClick = { accountId -> backStack.add(accountRoute(accountId)) },
        )
        accountEntry(
            onSendClick = { accountId -> backStack.add(transferRecipientRoute(accountId)) },
            onBack = { backStack.removeLastOrNull() },
        )
        transferRecipientEntry(
            onBack = { backStack.removeLastOrNull() },
            onAccountNumberInput = { sourceId ->
                backStack.add(TransferAccountInputRoute(sourceId))
            },
            onAmountInput = { route -> backStack.add(route) },
        )
        transferAccountInputEntry(
            onBack = { backStack.removeLastOrNull() },
            onResolved = { route -> backStack.add(route) },
        )
        transferAmountEntry(
            onBack = { backStack.removeLastOrNull() },
            onNext = { route -> backStack.add(route) },
        )
        transferConfirmEntry(
            onBack = { backStack.removeLastOrNull() },
            onSent = { route -> backStack.add(route) },
        )
        transferResultEntry(
            // 송금 완료 후 "확인"/상단 백 → 송금 플로우(수취인~결과) 전체를 걷어내고 출금계좌 상세로 복귀.
            // 그 화면은 Room Flow를 구독하므로 송금이 갱신한 잔액·거래내역이 즉시 반영된다.
            onFinish = { sourceAccountId ->
                val anchor = backStack.lastIndexOf(AccountRoute(sourceAccountId))
                if (anchor >= 0) {
                    while (backStack.lastIndex > anchor) backStack.removeAt(backStack.lastIndex)
                }
            },
        )
    }
