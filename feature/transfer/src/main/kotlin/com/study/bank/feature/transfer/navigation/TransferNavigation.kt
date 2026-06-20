package com.study.bank.feature.transfer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.transfer.amount.ui.AmountRoute
import com.study.bank.feature.transfer.confirm.ui.ConfirmRoute
import com.study.bank.feature.transfer.recipient.ui.RecipientRoute
import com.study.bank.feature.transfer.result.ui.ResultRoute

const val TRANSFER_ACCOUNT_ID_ARG = "accountId"
const val TRANSFER_RECIPIENT_ID_ARG = "recipientId"
const val TRANSFER_AMOUNT_ARG = "amount"

/** 송금 플로우 진입(1번째 화면: 수취인 선택). {accountId}는 출금계좌. */
const val TRANSFER_ROUTE = "transfer/{$TRANSFER_ACCOUNT_ID_ARG}"

/** 송금 2번째 화면: 금액 입력. {accountId}=출금계좌, {recipientId}=수취계좌. */
const val TRANSFER_AMOUNT_ROUTE = "transfer/{$TRANSFER_ACCOUNT_ID_ARG}/amount/{$TRANSFER_RECIPIENT_ID_ARG}"

/** 송금 3번째 화면: 송금 확인. {amount}=출금계좌 통화 정수 금액. */
const val TRANSFER_CONFIRM_ROUTE =
    "transfer/{$TRANSFER_ACCOUNT_ID_ARG}/confirm/{$TRANSFER_RECIPIENT_ID_ARG}/{$TRANSFER_AMOUNT_ARG}"

/** 송금 4번째 화면: 송금 결과(로딩 → 성공/실패). 진입과 동시에 실제 송금을 실행한다. */
const val TRANSFER_RESULT_ROUTE =
    "transfer/{$TRANSFER_ACCOUNT_ID_ARG}/result/{$TRANSFER_RECIPIENT_ID_ARG}/{$TRANSFER_AMOUNT_ARG}"

fun NavController.navigateToTransfer(sourceAccountId: AccountId, navOptions: NavOptions? = null) {
    navigate("transfer/${sourceAccountId.value}", navOptions)
}

fun NavController.navigateToTransferAmount(
    sourceAccountId: String,
    recipientAccountId: String,
    navOptions: NavOptions? = null,
) {
    navigate("transfer/$sourceAccountId/amount/$recipientAccountId", navOptions)
}

fun NavController.navigateToTransferConfirm(
    sourceAccountId: String,
    recipientAccountId: String,
    amount: Long,
    navOptions: NavOptions? = null,
) {
    navigate("transfer/$sourceAccountId/confirm/$recipientAccountId/$amount", navOptions)
}

fun NavController.navigateToTransferResult(
    sourceAccountId: String,
    recipientAccountId: String,
    amount: Long,
    navOptions: NavOptions? = null,
) {
    navigate("transfer/$sourceAccountId/result/$recipientAccountId/$amount", navOptions)
}

fun NavGraphBuilder.transferScreen(
    onBack: () -> Unit,
    onAccountNumberInput: () -> Unit,
    onAmountInput: (sourceAccountId: String, recipientAccountId: String) -> Unit,
) {
    composable(
        route = TRANSFER_ROUTE,
        arguments = listOf(navArgument(TRANSFER_ACCOUNT_ID_ARG) { type = NavType.StringType }),
    ) {
        RecipientRoute(
            onBack = onBack,
            onAccountNumberInput = onAccountNumberInput,
            onContinue = onAmountInput,
        )
    }
}

fun NavGraphBuilder.transferAmountScreen(
    onBack: () -> Unit,
    onNext: (sourceAccountId: String, recipientAccountId: String, amount: Long) -> Unit,
) {
    composable(
        route = TRANSFER_AMOUNT_ROUTE,
        arguments = listOf(
            navArgument(TRANSFER_ACCOUNT_ID_ARG) { type = NavType.StringType },
            navArgument(TRANSFER_RECIPIENT_ID_ARG) { type = NavType.StringType },
        ),
    ) {
        AmountRoute(onBack = onBack, onNext = onNext)
    }
}

fun NavGraphBuilder.transferConfirmScreen(
    onBack: () -> Unit,
    onSent: (sourceAccountId: String, recipientAccountId: String, amount: Long) -> Unit,
) {
    composable(
        route = TRANSFER_CONFIRM_ROUTE,
        arguments = listOf(
            navArgument(TRANSFER_ACCOUNT_ID_ARG) { type = NavType.StringType },
            navArgument(TRANSFER_RECIPIENT_ID_ARG) { type = NavType.StringType },
            navArgument(TRANSFER_AMOUNT_ARG) { type = NavType.LongType },
        ),
    ) {
        ConfirmRoute(onBack = onBack, onSent = onSent)
    }
}

fun NavGraphBuilder.transferResultScreen(
    onFinish: () -> Unit,
) {
    composable(
        route = TRANSFER_RESULT_ROUTE,
        arguments = listOf(
            navArgument(TRANSFER_ACCOUNT_ID_ARG) { type = NavType.StringType },
            navArgument(TRANSFER_RECIPIENT_ID_ARG) { type = NavType.StringType },
            navArgument(TRANSFER_AMOUNT_ARG) { type = NavType.LongType },
        ),
    ) {
        ResultRoute(onFinish = onFinish)
    }
}
