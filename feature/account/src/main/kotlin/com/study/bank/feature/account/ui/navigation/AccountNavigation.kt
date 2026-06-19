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

fun NavController.navigateToAccount(accountId: AccountId, navOptions: NavOptions? = null) {
    navigate("account/${accountId.value}", navOptions)
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
