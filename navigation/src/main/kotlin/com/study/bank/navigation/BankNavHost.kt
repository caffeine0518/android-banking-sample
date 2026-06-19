package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.study.bank.feature.account.ui.navigation.accountScreen
import com.study.bank.feature.account.ui.navigation.navigateToAccount
import com.study.bank.feature.home.ui.navigation.HOME_ROUTE
import com.study.bank.feature.home.ui.navigation.homeScreen
import com.study.bank.feature.transfer.navigation.navigateToTransfer
import com.study.bank.feature.transfer.navigation.transferScreen

@Composable
fun BankNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
    ) {
        homeScreen(
            onAccountClick = { accountId -> navController.navigateToAccount(accountId) },
        )
        accountScreen(
            onSendClick = { accountId -> navController.navigateToTransfer(accountId) },
            onBack = { navController.popBackStack() },
        )
        transferScreen(
            onBack = { navController.popBackStack() },
            onAccountNumberInput = { /* 계좌번호 입력 화면 추가 시 연결 */ },
            onContinue = { /* 송금 다음 화면(금액 입력) 추가 시 연결 */ },
        )
    }
}
