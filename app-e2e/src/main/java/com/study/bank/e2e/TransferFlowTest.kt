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
import com.study.bank.core.ui.testing.BankTestTags.RESULT_FAILURE
import com.study.bank.core.ui.testing.BankTestTags.RESULT_SUCCESS
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_AMOUNT
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_RECIPIENT
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TransferFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 같은_통화_내_계좌로_송금하면_성공_화면이_보인다() {
        val (source, recipient) = AccountsByCurrency.sameCurrencyPair(Currency.KRW)
        openAmountScreen(sourceId = source, recipientId = recipient)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        composeRule.awaitTag(RESULT_SUCCESS)
    }

    @Test
    fun 통화가_다른_계좌로는_송금이_거절된다() {
        val (source, recipient) = AccountsByCurrency.crossCurrencyPair(from = Currency.KRW, to = Currency.USD)
        openAmountScreen(sourceId = source, recipientId = recipient)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        // mock 서버가 통화 불일치로 거절한다.
        composeRule.awaitTag(RESULT_FAILURE)
    }

    @Test
    fun 송금_성공_후_확인하면_출금계좌_상세로_돌아간다() {
        val (source, recipient) = AccountsByCurrency.sameCurrencyPair(Currency.KRW)
        openAmountScreen(sourceId = source, recipientId = recipient)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        composeRule.awaitTag(RESULT_SUCCESS)

        // 확인 화면으로 돌아가 재송금하지 못하도록 시스템 뒤로가기가 차단된다.
        pressSystemBack()
        composeRule.onNodeWithTag(RESULT_SUCCESS).assertIsDisplayed()

        composeRule.onNodeWithTag(RESULT_CONFIRM).performClick()
        composeRule.awaitTag(accountDetail(source))
        composeRule.onNodeWithTag(RESULT_SUCCESS).assertDoesNotExist()
    }

    @Test
    fun 같은_USD_계좌로_소수점_금액을_보내면_절삭_없이_송금된다() {
        val (source, recipient) = AccountsByCurrency.sameCurrencyPair(Currency.USD)
        openAmountScreen(sourceId = source, recipientId = recipient)
        // $100.50 = 10,050센트
        enterDigits("10050")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        // 입력으로부터 계산된 값이라 텍스트로 단언한다. 옛 코드는 10050을 $10,050로 해석했다.
        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithText("$100.50", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        composeRule.awaitTag(RESULT_SUCCESS)
    }

    /** 홈 → [sourceId] 상세 → 보내기 → 수취인 [recipientId] → 금액 화면. */
    private fun openAmountScreen(sourceId: String, recipientId: String) {
        composeRule.awaitTag(accountItem(sourceId))
        composeRule.onNodeWithTag(accountItem(sourceId)).performClick()

        composeRule.awaitTag(accountDetail(sourceId))
        composeRule.onNodeWithTag(DETAIL_SEND).performClick()

        composeRule.awaitTag(SCREEN_RECIPIENT)
        composeRule.onNodeWithTag(accountItem(recipientId)).performClick()

        composeRule.awaitTag(SCREEN_AMOUNT)
    }

    /** 커스텀 키패드로 [digits]를 한 자리씩 입력. 각 숫자 키는 화면에서 유일한 동일 텍스트 노드다. */
    private fun enterDigits(digits: String) {
        digits.forEach { composeRule.onNodeWithText(it.toString()).performClick() }
    }

    private fun pressSystemBack() {
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }
}
