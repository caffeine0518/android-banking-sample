package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.study.bank.feature.account.ui.navigation.accountScreen
import com.study.bank.feature.account.ui.navigation.navigateToAccount
import com.study.bank.feature.home.ui.navigation.HOME_ROUTE
import com.study.bank.feature.home.ui.navigation.homeScreen
import com.study.bank.feature.transfer.navigation.navigateToTransfer
import com.study.bank.feature.transfer.navigation.navigateToTransferAmount
import com.study.bank.feature.transfer.navigation.navigateToTransferConfirm
import com.study.bank.feature.transfer.navigation.transferAmountScreen
import com.study.bank.feature.transfer.navigation.transferConfirmScreen
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
            onAmountInput = { sourceId, recipientId ->
                navController.navigateToTransferAmount(sourceId, recipientId)
            },
        )
        transferAmountScreen(
            onBack = { navController.popBackStack() },
            onNext = { sourceId, recipientId, amount ->
                navController.navigateToTransferConfirm(sourceId, recipientId, amount)
            },
        )
        transferConfirmScreen(
            onBack = { navController.popBackStack() },
            onSent = { /* 비밀번호/송금 실행·완료 화면 추가 시 연결 */ },
        )
    }
}
