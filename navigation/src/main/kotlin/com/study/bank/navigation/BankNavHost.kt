package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.study.bank.feature.home.navigation.HOME_ROUTE
import com.study.bank.feature.home.navigation.homeScreen

@Composable
fun BankNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
    ) {
        homeScreen(
            onAccountClick = { /* account detail destination 추가 시 연결 */ },
        )
    }
}
