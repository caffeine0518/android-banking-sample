package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.study.bank.feature.account.ui.navigation.accountScreen
import com.study.bank.feature.account.ui.navigation.navigateToAccount
import com.study.bank.feature.home.ui.navigation.HOME_ROUTE
import com.study.bank.feature.home.ui.navigation.homeScreen
import com.study.bank.feature.transfer.navigation.navigateToTransfer
import com.study.bank.feature.transfer.navigation.navigateToTransferAccountInput
import com.study.bank.feature.transfer.navigation.navigateToTransferAmount
import com.study.bank.feature.transfer.navigation.navigateToTransferConfirm
import com.study.bank.feature.transfer.navigation.navigateToTransferResult
import com.study.bank.feature.transfer.navigation.transferAccountInputScreen
import com.study.bank.feature.transfer.navigation.transferAmountScreen
import com.study.bank.feature.transfer.navigation.transferConfirmScreen
import com.study.bank.feature.transfer.navigation.transferResultScreen
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
            onAccountNumberInput = { sourceId ->
                navController.navigateToTransferAccountInput(sourceId)
            },
            onAmountInput = { sourceId, recipientId ->
                navController.navigateToTransferAmount(sourceId, recipientId)
            },
        )
        transferAccountInputScreen(
            onBack = { navController.popBackStack() },
            onResolved = { sourceId, recipientId ->
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
            onSent = { sourceId, recipientId, amount ->
                navController.navigateToTransferResult(sourceId, recipientId, amount)
            },
        )
        transferResultScreen(
            // 송금 완료 후 "확인"/백 → 송금 플로우 전체를 걷어내고 홈으로 복귀.
            onFinish = { navController.popBackStack(HOME_ROUTE, inclusive = false) },
        )
    }
}
