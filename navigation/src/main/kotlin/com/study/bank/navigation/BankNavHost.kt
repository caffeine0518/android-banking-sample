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
        onBack = { backStack.pop() },
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
            onAccountClick = { accountId -> backStack.push(accountRoute(accountId)) },
        )
        accountEntry(
            onSendClick = { accountId -> backStack.push(transferRecipientRoute(accountId)) },
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
 * 중복 push 가드. 클릭은 백스택을 그 자리에서 바꾸지 않고 intent→effect→콜백의 여러 코루틴
 * 단계를 거쳐 처리되므로, 화면이 아직 안 바뀐 사이에 더블탭이 끼어들면 같은 키가 두 번 쌓인다 —
 * 두 엔트리는 contentKey를 공유해 상태·VM이 섞이고 뒤로가기도 2번 필요해지므로, 최상단과 같은 키는 무시한다.
 */
private fun NavBackStack<NavKey>.push(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

/**
 * pop 플로어 가드. 백버튼 더블탭이 removeLastOrNull을 두 번 태우면 백스택이 비어 NavDisplay가
 * 즉시 죽는다("NavDisplay backstack cannot be empty" — 에뮬레이터 실증). 루트 엔트리는 항상 남긴다.
 */
private fun NavBackStack<NavKey>.pop() {
    if (size > 1) removeLastOrNull()
}

/**
 * popUpTo. [predicate]에 처음 걸리는(위에서 아래로) 엔트리가 최상단이 되도록 그 위를 모두 걷어낸다
 * (Nav2 popUpTo, inclusive=false 대응). 앵커를 값이 아니라 식별 필드로 참조하므로 라우트에 필드가
 * 늘어도 매칭이 깨지지 않는다. 걸리는 엔트리가 없으면 스택 불변. 앵커는 남으므로 스택이 빌 수 없고, 재호출도 멱등이다.
 */
private inline fun NavBackStack<NavKey>.popUpTo(predicate: (NavKey) -> Boolean) {
    val index = indexOfLast(predicate)
    if (index < 0) return
    while (lastIndex > index) removeAt(lastIndex)
}
