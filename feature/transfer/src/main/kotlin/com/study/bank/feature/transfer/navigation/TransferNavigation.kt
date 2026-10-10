package com.study.bank.feature.transfer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.accountinput.ui.AccountInputRoute
import com.study.bank.feature.transfer.amount.ui.AmountRoute
import com.study.bank.feature.transfer.confirm.ui.ConfirmRoute
import com.study.bank.feature.transfer.recipient.ui.RecipientRoute
import com.study.bank.feature.transfer.result.ui.ResultRoute
import kotlinx.serialization.Serializable

/** 실명조회나 내 계좌 선택에서 확정된 수취인. 외부 계좌는 저장소에 없으므로 재조회 없이 결과 화면까지 전달한다. */
@Serializable
data class TransferRecipientArg(
    val bankCode: String,
    val accountNumber: String,
    val holderName: String,
) {
    val bankDisplayName: String get() = BankCode.byCode(bankCode)?.displayName ?: bankCode
}

@Serializable
data class TransferRecipientRoute(val sourceAccountId: String) : NavKey

@Serializable
data class TransferAccountInputRoute(val sourceAccountId: String) : NavKey

@Serializable
data class TransferAmountRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
) : NavKey

/** [amount]는 출금계좌 통화의 최소 단위 정수다. */
@Serializable
data class TransferConfirmRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
    val amount: Long,
) : NavKey

/** 진입 시 송금을 실행한다. */
@Serializable
data class TransferResultRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
    val amount: Long,
    val idempotencyKey: String,
) : NavKey

fun EntryProviderScope<NavKey>.transferRecipientEntry(
    onBack: () -> Unit,
    onAccountNumberInput: (sourceAccountId: String) -> Unit,
    onAmountInput: (TransferAmountRoute) -> Unit,
) {
    entry<TransferRecipientRoute> { key ->
        RecipientRoute(
            route = key,
            onBack = onBack,
            onAccountNumberInput = onAccountNumberInput,
            onContinue = onAmountInput,
        )
    }
}

fun EntryProviderScope<NavKey>.transferAccountInputEntry(
    onBack: () -> Unit,
    onResolved: (TransferAmountRoute) -> Unit,
) {
    entry<TransferAccountInputRoute> { key ->
        AccountInputRoute(route = key, onBack = onBack, onResolved = onResolved)
    }
}

fun EntryProviderScope<NavKey>.transferAmountEntry(
    onBack: () -> Unit,
    onNext: (TransferConfirmRoute) -> Unit,
) {
    entry<TransferAmountRoute> { key ->
        AmountRoute(route = key, onBack = onBack, onNext = onNext)
    }
}

fun EntryProviderScope<NavKey>.transferConfirmEntry(
    onBack: () -> Unit,
    onSent: (TransferResultRoute) -> Unit,
) {
    entry<TransferConfirmRoute> { key ->
        ConfirmRoute(route = key, onBack = onBack, onSent = onSent)
    }
}

fun EntryProviderScope<NavKey>.transferResultEntry(
    onFinish: (sourceAccountId: String) -> Unit,
) {
    entry<TransferResultRoute> { key ->
        ResultRoute(route = key, onFinish = onFinish)
    }
}
