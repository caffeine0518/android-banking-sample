package com.study.bank.feature.transfer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.transfer.recipient.ui.RecipientRoute

const val TRANSFER_ACCOUNT_ID_ARG = "accountId"

/** 송금 플로우 진입(1번째 화면: 수취인 선택). {accountId}는 출금계좌. */
const val TRANSFER_ROUTE = "transfer/{$TRANSFER_ACCOUNT_ID_ARG}"

fun NavController.navigateToTransfer(sourceAccountId: AccountId, navOptions: NavOptions? = null) {
    navigate("transfer/${sourceAccountId.value}", navOptions)
}

fun NavGraphBuilder.transferScreen(
    onBack: () -> Unit,
    onAccountNumberInput: () -> Unit,
    onContinue: () -> Unit,
) {
    composable(
        route = TRANSFER_ROUTE,
        arguments = listOf(navArgument(TRANSFER_ACCOUNT_ID_ARG) { type = NavType.StringType }),
    ) {
        RecipientRoute(
            onBack = onBack,
            onAccountNumberInput = onAccountNumberInput,
            onContinue = onContinue,
        )
    }
}
