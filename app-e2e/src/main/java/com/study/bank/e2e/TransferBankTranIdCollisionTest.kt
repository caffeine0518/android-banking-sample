package com.study.bank.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.AMOUNT_NEXT
import com.study.bank.core.ui.testing.BankTestTags.CONFIRM_SEND
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_SEND
import com.study.bank.core.ui.testing.BankTestTags.RESULT_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.RESULT_SUCCESS
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_AMOUNT
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_RECIPIENT
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.data.di.transfer.IdempotencyKeyModule
import com.study.bank.domain.model.Currency
import com.study.bank.domain.model.transfer.IdempotencyKeyGenerator
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

/**
 * 해시가 같은 멱등성 키로 송금해도 거래고유번호(bank_tran_id)가 충돌하지 않는지 화면과 잔액으로 검증하는 E2E.
 *
 * 송금마다 해시가 같은 멱등성 키를 발급한다. 번호를 키의 해시로 만들면 두 번째 송금이 첫 번째 송금과 같은 번호로
 * 나가 서버가 거절한다. 번호는 키마다 순번으로 발급되므로 두 송금 모두 성공해야 한다.
 */
@HiltAndroidTest
@UninstallModules(IdempotencyKeyModule::class)
class TransferBankTranIdCollisionTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val collidingKeys = listOf(COLLIDING_KEY_A, COLLIDING_KEY_B).iterator()

    @BindValue
    val idempotencyKeyGenerator = IdempotencyKeyGenerator { collidingKeys.next() }

    @Test
    fun 거래고유번호가_앞_송금과_충돌해도_성공_화면이_보이고_두_송금만큼_차감된다() {
        val (source, recipient) = AccountsByCurrency.sameCurrencyPair(Currency.KRW)
        composeRule.awaitTag(accountItem(source))
        composeRule.onNodeWithTag(accountItem(source)).performClick()

        send(sourceId = source, recipientId = recipient, digits = FIRST_AMOUNT_DIGITS)
        send(sourceId = source, recipientId = recipient, digits = SECOND_AMOUNT_DIGITS)

        composeRule.onNodeWithText(BALANCE_AFTER_BOTH_SENDS, substring = true).assertIsDisplayed()
    }

    /** [sourceId] 상세 → 보내기 → 수취인 [recipientId] → 금액 [digits] → 확인 → 성공 → 상세로 복귀까지. */
    private fun send(sourceId: String, recipientId: String, digits: String) {
        composeRule.awaitTag(accountDetail(sourceId))
        composeRule.onNodeWithTag(DETAIL_SEND).performClick()

        composeRule.awaitTag(SCREEN_RECIPIENT)
        composeRule.onNodeWithTag(accountItem(recipientId)).performClick()

        composeRule.awaitTag(SCREEN_AMOUNT)
        digits.forEach { composeRule.onNodeWithText(it.toString()).performClick() }
        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()
        composeRule.awaitTag(RESULT_SUCCESS)

        composeRule.onNodeWithTag(RESULT_CONFIRM).performClick()
        composeRule.awaitTag(accountDetail(sourceId))
    }

    private companion object {
        // String.hashCode 가 같은 두 키. 해시로 bank_tran_id 를 만들면 같은 번호가 된다.
        const val COLLIDING_KEY_A = "e2e-collision-Aa"
        const val COLLIDING_KEY_B = "e2e-collision-BB"

        const val FIRST_AMOUNT_DIGITS = "10000"
        const val SECOND_AMOUNT_DIGITS = "30000"

        // KRW 첫 시드 계좌 2,847,320원 - 10,000 - 30,000. 입력으로부터 계산된 값이라 텍스트로 단언한다.
        const val BALANCE_AFTER_BOTH_SENDS = "2,807,320"
    }
}
