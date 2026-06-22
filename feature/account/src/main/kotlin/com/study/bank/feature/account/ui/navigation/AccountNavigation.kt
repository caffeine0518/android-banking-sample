package com.study.bank.feature.account.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.account.ui.AccountDetailRoute

const val ACCOUNT_ID_ARG = "accountId"
const val ACCOUNT_ROUTE = "account/{$ACCOUNT_ID_ARG}"

/**
 * [accountId] 상세 화면의 인자 채워진 구체 라우트. 진입(navigate)과 복귀(popBackStack) 양쪽에서 같은
 * 규칙으로 만들어, 백스택의 동일 목적지를 식별할 수 있게 한다.
 */
fun accountRoute(accountId: String): String = "account/$accountId"

fun NavController.navigateToAccount(accountId: AccountId, navOptions: NavOptions? = null) {
    navigate(accountRoute(accountId.value), navOptions)
}

fun NavGraphBuilder.accountScreen(
    onSendClick: (AccountId) -> Unit,
    onBack: () -> Unit,
) {
    composable(
        route = ACCOUNT_ROUTE,
        arguments = listOf(navArgument(ACCOUNT_ID_ARG) { type = NavType.StringType }),
    ) {
        AccountDetailRoute(onSendClick = onSendClick, onBack = onBack)
    }
}
