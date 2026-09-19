package com.study.bank.feature.transfer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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
 * Nav3 내비 키는 kotlinx 직렬화로 통째 저장되므로 [TransferAmountRoute] 등에 중첩 필드로 그대로 포함한다.
 */
@Serializable
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
data class TransferRecipientRoute(val sourceAccountId: String) : NavKey

/** 송금 1-b 화면: 계좌번호 직접 입력(외부 수취계좌). [sourceAccountId]=출금계좌. */
@Serializable
data class TransferAccountInputRoute(val sourceAccountId: String) : NavKey

/** 송금 2번째 화면: 금액 입력. 수취인 신원을 함께 포함한다. */
@Serializable
data class TransferAmountRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
) : NavKey

/** 송금 3번째 화면: 송금 확인. [amount]=출금계좌 통화 최소단위(minor unit) 정수. */
@Serializable
data class TransferConfirmRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
    val amount: Long,
) : NavKey

/** 송금 4번째 화면: 송금 결과(로딩→성공/실패). 진입과 동시에 실제 송금을 실행한다. */
@Serializable
data class TransferResultRoute(
    val sourceAccountId: String,
    val recipient: TransferRecipientArg,
    val amount: Long,
) : NavKey

fun transferRecipientRoute(sourceAccountId: AccountId) = TransferRecipientRoute(sourceAccountId.value)

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
