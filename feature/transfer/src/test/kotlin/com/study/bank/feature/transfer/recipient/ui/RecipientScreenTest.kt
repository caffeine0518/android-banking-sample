package com.study.bank.feature.transfer.recipient.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.recipient.contract.RecipientIntent
import com.study.bank.feature.transfer.recipient.contract.RecipientState
import com.study.bank.feature.transfer.recipient.ui.model.AccountTypeUi
import com.study.bank.feature.transfer.recipient.ui.model.AccountUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RecipientScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<RecipientIntent>()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setScreen(state: RecipientState) {
        composeRule.setContent {
            MaterialTheme {
                RecipientScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    @Test
    fun `제목과 계좌번호 입력 버튼, 내 계좌가 표시된다`() {
        setScreen(RecipientState(myAccounts = listOf(account("acc-2", "세이프박스"))))

        composeRule.onNodeWithText(string(R.string.transfer_recipient_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_account_number_hint)).assertIsDisplayed()
        composeRule.onNodeWithText("세이프박스").assertIsDisplayed()
    }

    @Test
    fun `계좌번호 입력 버튼을 누르면 AccountNumberInputClicked 인텐트가 방출된다`() {
        setScreen(RecipientState())

        composeRule.onNodeWithText(string(R.string.transfer_account_number_hint)).performClick()

        assertEquals(listOf(RecipientIntent.AccountNumberInputClicked), intents)
    }

    @Test
    fun `내 계좌를 누르면 해당 accountId로 MyAccountClicked 인텐트가 방출된다`() {
        setScreen(RecipientState(myAccounts = listOf(account("acc-2", "세이프박스"))))

        // Row의 clickable은 자식 텍스트를 머지하지 않으므로 텍스트 노드를 직접 탭 → clickable 조상이 처리.
        composeRule.onNodeWithText("세이프박스").performClick()

        assertEquals(listOf(RecipientIntent.MyAccountClicked("acc-2")), intents)
    }

    @Test
    fun `백 버튼을 누르면 BackClicked 인텐트가 방출된다`() {
        setScreen(RecipientState())

        composeRule.onNodeWithContentDescription(string(R.string.transfer_action_back)).performClick()

        assertEquals(listOf(RecipientIntent.BackClicked), intents)
    }

    private fun account(id: String, nickname: String) = AccountUi(
        id = id,
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = nickname,
        numberMasked = "1000-55-***4443",
    )
}
