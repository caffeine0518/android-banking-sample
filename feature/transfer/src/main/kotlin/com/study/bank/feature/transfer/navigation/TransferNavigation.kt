package com.study.bank.feature.transfer.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.study.bank.domain.model.BankCode
import com.study.bank.domain.model.account.AccountId
import com.study.bank.feature.transfer.accountinput.ui.AccountInputRoute
import com.study.bank.feature.transfer.amount.ui.AmountRoute
import com.study.bank.feature.transfer.confirm.ui.ConfirmRoute
import com.study.bank.feature.transfer.recipient.ui.RecipientRoute
import com.study.bank.feature.transfer.result.ui.ResultRoute
import kotlinx.serialization.Serializable

/**
 * 송금 플로우 동안 화면 사이를 흐르는 수취인 신원. 실명조회(외부 계좌)나 "내 계좌" 선택(picker)에서 한 번
 * 확정돼 금액→확인→결과까지 그대로 전달된다 — 출금계좌 저장소에 없는 외부 계좌도 식별자 재조회 없이 처리된다.
 * 타입세이프 라우트에는 기본 타입만 싣도록 [TransferAmountRoute] 등에서 평탄화해 담는다.
 */
data class TransferRecipientArg(
    val bankCode: String,
    val accountNumber: String,
    val holderName: String,
) {
    /** 표시용 은행명. 알 수 없는 코드는 코드 원문으로 폴백. */
    val bankDisplayName: String get() = BankCode.byCode(bankCode)?.displayName ?: bankCode
}

/** 송금 1번째 화면: 수취인 선택. [sourceAccountId]=출금계좌. */
@Serializable
data class TransferRecipientRoute(val sourceAccountId: String)

/** 송금 1-b 화면: 계좌번호 직접 입력(외부 수취계좌). [sourceAccountId]=출금계좌. */
@Serializable
data class TransferAccountInputRoute(val sourceAccountId: String)

/** 송금 2번째 화면: 금액 입력. 수취인 신원을 함께 싣는다. */
@Serializable
data class TransferAmountRoute(
    val sourceAccountId: String,
    val recipientBankCode: String,
    val recipientAccountNumber: String,
    val recipientHolderName: String,
)

/** 송금 3번째 화면: 송금 확인. [amount]=출금계좌 통화 최소단위(minor unit) 정수. */
@Serializable
data class TransferConfirmRoute(
    val sourceAccountId: String,
    val recipientBankCode: String,
    val recipientAccountNumber: String,
    val recipientHolderName: String,
    val amount: Long,
)

/** 송금 4번째 화면: 송금 결과(로딩→성공/실패). 진입과 동시에 실제 송금을 실행한다. */
@Serializable
data class TransferResultRoute(
    val sourceAccountId: String,
    val recipientBankCode: String,
    val recipientAccountNumber: String,
    val recipientHolderName: String,
    val amount: Long,
)

// 타입세이프 라우트는 그래프(composable<T>/navigate(route))에서 안전하게 쓰고, 화면 ViewModel은
// SavedStateHandle에 풀려 있는 라우트 필드를 필드명 키로 읽는다(런타임·단위테스트 모두 안전). 키는 라우트
// 필드명과 동일하다 — 여기 한곳에 모아 둔다.
const val ARG_SOURCE_ACCOUNT_ID = "sourceAccountId"
const val ARG_AMOUNT = "amount"
private const val ARG_RECIPIENT_BANK_CODE = "recipientBankCode"
private const val ARG_RECIPIENT_ACCOUNT_NUMBER = "recipientAccountNumber"
private const val ARG_RECIPIENT_HOLDER_NAME = "recipientHolderName"

/** 라우트가 SavedStateHandle에 풀어둔 수취인 신원 필드를 묶어 읽는다. */
fun SavedStateHandle.transferRecipientArg(): TransferRecipientArg = TransferRecipientArg(
    bankCode = checkNotNull(get<String>(ARG_RECIPIENT_BANK_CODE)) { "recipientBankCode 인자 누락" },
    accountNumber = checkNotNull(get<String>(ARG_RECIPIENT_ACCOUNT_NUMBER)) { "recipientAccountNumber 인자 누락" },
    holderName = checkNotNull(get<String>(ARG_RECIPIENT_HOLDER_NAME)) { "recipientHolderName 인자 누락" },
)

fun amountRoute(sourceAccountId: String, recipient: TransferRecipientArg) = TransferAmountRoute(
    sourceAccountId = sourceAccountId,
    recipientBankCode = recipient.bankCode,
    recipientAccountNumber = recipient.accountNumber,
    recipientHolderName = recipient.holderName,
)

fun confirmRoute(sourceAccountId: String, recipient: TransferRecipientArg, amount: Long) =
    TransferConfirmRoute(
        sourceAccountId = sourceAccountId,
        recipientBankCode = recipient.bankCode,
        recipientAccountNumber = recipient.accountNumber,
        recipientHolderName = recipient.holderName,
        amount = amount,
    )

fun resultRoute(sourceAccountId: String, recipient: TransferRecipientArg, amount: Long) =
    TransferResultRoute(
        sourceAccountId = sourceAccountId,
        recipientBankCode = recipient.bankCode,
        recipientAccountNumber = recipient.accountNumber,
        recipientHolderName = recipient.holderName,
        amount = amount,
    )

fun NavController.navigateToTransfer(sourceAccountId: AccountId, navOptions: NavOptions? = null) {
    navigate(TransferRecipientRoute(sourceAccountId.value), navOptions)
}

fun NavController.navigateToTransferAccountInput(sourceAccountId: String) {
    navigate(TransferAccountInputRoute(sourceAccountId))
}

fun NavController.navigateToTransferAmount(route: TransferAmountRoute) = navigate(route)

fun NavController.navigateToTransferConfirm(route: TransferConfirmRoute) = navigate(route)

fun NavController.navigateToTransferResult(route: TransferResultRoute) = navigate(route)

fun NavGraphBuilder.transferScreen(
    onBack: () -> Unit,
    onAccountNumberInput: (sourceAccountId: String) -> Unit,
    onAmountInput: (TransferAmountRoute) -> Unit,
) {
    composable<TransferRecipientRoute> {
        RecipientRoute(
            onBack = onBack,
            onAccountNumberInput = onAccountNumberInput,
            onContinue = onAmountInput,
        )
    }
}

fun NavGraphBuilder.transferAccountInputScreen(
    onBack: () -> Unit,
    onResolved: (TransferAmountRoute) -> Unit,
) {
    composable<TransferAccountInputRoute> {
        AccountInputRoute(onBack = onBack, onResolved = onResolved)
    }
}

fun NavGraphBuilder.transferAmountScreen(
    onBack: () -> Unit,
    onNext: (TransferConfirmRoute) -> Unit,
) {
    composable<TransferAmountRoute> {
        AmountRoute(onBack = onBack, onNext = onNext)
    }
}

fun NavGraphBuilder.transferConfirmScreen(
    onBack: () -> Unit,
    onSent: (TransferResultRoute) -> Unit,
) {
    composable<TransferConfirmRoute> {
        ConfirmRoute(onBack = onBack, onSent = onSent)
    }
}

fun NavGraphBuilder.transferResultScreen(
    onFinish: () -> Unit,
) {
    composable<TransferResultRoute> {
        ResultRoute(onFinish = onFinish)
    }
}
