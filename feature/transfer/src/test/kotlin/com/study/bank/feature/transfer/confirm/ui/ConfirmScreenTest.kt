package com.study.bank.feature.transfer.confirm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.confirm.contract.ConfirmIntent
import com.study.bank.feature.transfer.confirm.contract.ConfirmState
import com.study.bank.feature.transfer.confirm.ui.model.ConfirmDetailUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// 제목(가중치 배치) + bottomBar 버튼이 함께 보이도록 실제 폰 크기를 준다.
@Config(qualifiers = "w411dp-h891dp")
class ConfirmScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<ConfirmIntent>()

    private fun string(id: Int, vararg args: Any) =
        RuntimeEnvironment.getApplication().getString(id, *args)

    private fun setScreen(state: ConfirmState) {
        composeRule.setContent {
            MaterialTheme {
                ConfirmScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    @Test
    fun `제목·금액·정보행·보내기 버튼이 표시된다`() {
        setScreen(ConfirmState(detail = detail()))

        composeRule.onNodeWithText("안성재", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_confirm_title_question)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_confirm_label_display_name)).assertIsDisplayed()
        composeRule.onNodeWithText("강남규").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_confirm_label_deposit)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_confirm_send)).assertIsDisplayed()
    }

    @Test
    fun `보내기 버튼을 누르면 SendClicked 인텐트가 방출된다`() {
        setScreen(ConfirmState(detail = detail()))

        composeRule.onNodeWithText(string(R.string.transfer_confirm_send)).performClick()

        assertEquals(listOf(ConfirmIntent.SendClicked), intents)
    }

    @Test
    fun `받는 분에게 표시 행을 누르면 DisplayNameClicked 인텐트가 방출된다`() {
        setScreen(ConfirmState(detail = detail()))

        composeRule.onNodeWithText("강남규").performClick()

        assertEquals(listOf(ConfirmIntent.DisplayNameClicked), intents)
    }

    @Test
    fun `백 버튼을 누르면 BackClicked 인텐트가 방출된다`() {
        setScreen(ConfirmState(detail = detail()))

        composeRule.onNodeWithContentDescription(string(R.string.transfer_action_back)).performClick()

        assertEquals(listOf(ConfirmIntent.BackClicked), intents)
    }

    @Test
    fun `detail이 없으면 보내기 버튼이 비활성이다`() {
        setScreen(ConfirmState(detail = null))

        composeRule.onNodeWithText(string(R.string.transfer_confirm_send)).assertIsNotEnabled()
    }

    private fun detail() = ConfirmDetailUi(
        recipientHolderName = "안성재",
        amount = MoneyUi(BigDecimal.valueOf(2), CurrencyUi.KRW),
        displayName = "강남규",
        sourceNickname = "U드림 저축예금 (인터넷전용)",
        sourceType = AccountTypeUi.SAVINGS,
        recipientBankDisplayName = "신한은행",
        recipientNumberMasked = "110-503-685417",
    )
}
