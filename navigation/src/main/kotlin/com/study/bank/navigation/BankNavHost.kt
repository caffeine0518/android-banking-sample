package com.study.bank.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.study.bank.feature.account.ui.navigation.accountRoute
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
            onAmountInput = { route -> navController.navigateToTransferAmount(route) },
        )
        transferAccountInputScreen(
            onBack = { navController.popBackStack() },
            onResolved = { route -> navController.navigateToTransferAmount(route) },
        )
        transferAmountScreen(
            onBack = { navController.popBackStack() },
            onNext = { route -> navController.navigateToTransferConfirm(route) },
        )
        transferConfirmScreen(
            onBack = { navController.popBackStack() },
            onSent = { route -> navController.navigateToTransferResult(route) },
        )
        transferResultScreen(
            // 송금 완료 후 "확인"/상단 백 → 송금 플로우(수취인~결과) 전체를 걷어내고 출금계좌 상세로 복귀.
            // 그 화면은 Room Flow를 구독하므로 송금이 갱신한 잔액·거래내역이 즉시 반영된다.
            onFinish = { sourceAccountId ->
                navController.popBackStack(accountRoute(sourceAccountId), inclusive = false)
            },
        )
    }
}
