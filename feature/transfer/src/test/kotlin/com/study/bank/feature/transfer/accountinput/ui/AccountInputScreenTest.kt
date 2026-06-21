package com.study.bank.feature.transfer.accountinput.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.study.bank.domain.model.BankCode
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.accountinput.contract.AccountInputError
import com.study.bank.feature.transfer.accountinput.contract.AccountInputIntent
import com.study.bank.feature.transfer.accountinput.contract.AccountInputState
import com.study.bank.feature.transfer.accountinput.ui.component.BankPickerSheet
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AccountInputScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<AccountInputIntent>()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setScreen(state: AccountInputState) {
        composeRule.setContent {
            MaterialTheme {
                AccountInputScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    @Test
    fun `제목·계좌번호 입력·선택된 은행이 표시된다`() {
        setScreen(AccountInputState(selectedBank = BankCode.KAKAO))

        composeRule.onNodeWithText(string(R.string.transfer_account_input_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_account_input_number_label)).assertIsDisplayed()
        composeRule.onNodeWithText(BankCode.KAKAO.displayName).assertIsDisplayed()
    }

    @Test
    fun `계좌번호를 입력하면 AccountNumberChanged 인텐트가 방출된다`() {
        setScreen(AccountInputState())

        composeRule.onNodeWithText(string(R.string.transfer_account_input_number_label))
            .performTextInput("868369666")

        assertEquals(listOf(AccountInputIntent.AccountNumberChanged("868369666")), intents)
    }

    @Test
    fun `지우기 버튼을 누르면 AccountNumberCleared 인텐트가 방출된다`() {
        setScreen(AccountInputState(accountNumber = "868369666"))

        composeRule.onNodeWithContentDescription(string(R.string.transfer_account_input_clear))
            .performClick()

        assertEquals(listOf(AccountInputIntent.AccountNumberCleared), intents)
    }

    @Test
    fun `은행 선택 영역을 누르면 BankSelectorClicked 인텐트가 방출된다`() {
        setScreen(AccountInputState(selectedBank = BankCode.KAKAO))

        composeRule.onNodeWithText(BankCode.KAKAO.displayName).performClick()

        assertEquals(listOf(AccountInputIntent.BankSelectorClicked), intents)
    }

    @Test
    fun `계좌번호가 있으면 확인 버튼이 보이고 누르면 ConfirmClicked 인텐트가 방출된다`() {
        setScreen(AccountInputState(accountNumber = "868369666"))

        composeRule.onNodeWithText(string(R.string.transfer_account_input_confirm)).performClick()

        assertEquals(listOf(AccountInputIntent.ConfirmClicked), intents)
    }

    @Test
    fun `오류가 있으면 오류 메시지가 표시된다`() {
        setScreen(AccountInputState(accountNumber = "868369666", error = AccountInputError.NOT_FOUND))

        composeRule.onNodeWithText(string(R.string.transfer_account_input_error_not_found))
            .assertIsDisplayed()
    }

    @Test
    fun `은행 셀을 누르면 해당 은행으로 onSelect가 호출된다`() {
        val selected = mutableListOf<BankCode>()
        composeRule.setContent {
            MaterialTheme {
                BankPickerSheet(
                    banks = BankCode.entries,
                    selected = BankCode.KAKAO,
                    onSelect = { selected += it },
                )
            }
        }

        composeRule.onNodeWithText(BankCode.SHINHAN.displayName).performClick()

        assertEquals(listOf(BankCode.SHINHAN), selected)
    }
}
