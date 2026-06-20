package com.study.bank.feature.transfer.amount.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.amount.contract.AmountIntent
import com.study.bank.feature.transfer.amount.contract.AmountState
import com.study.bank.feature.transfer.amount.ui.model.AmountRecipientUi
import com.study.bank.feature.transfer.amount.ui.model.AmountSourceUi
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
// 키패드 bottomBar가 길어 기본(작은) 뷰포트에선 콘텐츠 영역이 0높이로 눌린다. 실제 폰 크기를 준다.
@Config(qualifiers = "w411dp-h891dp")
class AmountScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<AmountIntent>()

    private fun string(id: Int, vararg args: Any) =
        RuntimeEnvironment.getApplication().getString(id, *args)

    private fun setScreen(state: AmountState) {
        composeRule.setContent {
            MaterialTheme {
                AmountScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    @Test
    fun `출금·수취 정보와 FROM·TO 라벨, 금액 힌트가 표시된다`() {
        setScreen(state(amount = 0))

        composeRule.onNodeWithText(string(R.string.transfer_amount_from_label)).assertIsDisplayed()
        composeRule.onNodeWithText("U드림 저축예금").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_amount_to_label)).assertIsDisplayed()
        composeRule.onNodeWithText("종합매매 계좌").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_amount_hint)).assertIsDisplayed()
    }

    @Test
    fun `숫자 키를 누르면 DigitAppended 인텐트가 방출된다`() {
        setScreen(state(amount = 0))

        composeRule.onNodeWithText("7").performClick()

        assertEquals(listOf(AmountIntent.DigitAppended("7")), intents)
    }

    @Test
    fun `지우기 키를 누르면 BackspacePressed 인텐트가 방출된다`() {
        setScreen(state(amount = 0))

        composeRule.onNodeWithContentDescription(string(R.string.transfer_amount_backspace))
            .performClick()

        assertEquals(listOf(AmountIntent.BackspacePressed), intents)
    }

    @Test
    fun `금액 입력 전에는 잔액 입력 칩을 누르면 FillBalanceClicked 인텐트가 방출된다`() {
        setScreen(state(amount = 0))

        composeRule.onNodeWithText(string(R.string.transfer_amount_fill_balance, "284,797원"))
            .performClick()

        assertEquals(listOf(AmountIntent.FillBalanceClicked), intents)
    }

    @Test
    fun `금액이 입력되면 다음 버튼이 표시되고 누르면 NextClicked 인텐트가 방출된다`() {
        setScreen(state(amount = 5))

        composeRule.onNodeWithText(string(R.string.transfer_amount_next)).performClick()

        assertEquals(listOf(AmountIntent.NextClicked), intents)
    }

    @Test
    fun `백 버튼을 누르면 BackClicked 인텐트가 방출된다`() {
        setScreen(state(amount = 0))

        composeRule.onNodeWithContentDescription(string(R.string.transfer_action_back)).performClick()

        assertEquals(listOf(AmountIntent.BackClicked), intents)
    }

    private fun state(amount: Long) = AmountState(
        source = AmountSourceUi(
            nickname = "U드림 저축예금",
            type = AccountTypeUi.SAVINGS,
            balance = MoneyUi(BigDecimal.valueOf(284_797), CurrencyUi.KRW),
        ),
        recipient = AmountRecipientUi(
            nickname = "종합매매 계좌",
            type = AccountTypeUi.CHECKING,
            bankDisplayName = "신한은행",
            numberMasked = "110-503-685417",
        ),
        amount = amount,
    )
}
