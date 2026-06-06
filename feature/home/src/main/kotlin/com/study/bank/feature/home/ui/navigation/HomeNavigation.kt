package com.study.bank.feature.home.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.home.ui.HomeRoute

const val HOME_ROUTE = "home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onAccountClick: (AccountId) -> Unit,
) {
    composable(HOME_ROUTE) {
        HomeRoute(onAccountClick = onAccountClick)
    }
}
