package com.study.bank.feature.home.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.home.R
import com.study.bank.feature.home.contract.HomeIntent
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.model.AccountTypeUi
import com.study.bank.feature.home.ui.model.AccountUi
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val emittedIntents = mutableListOf<HomeIntent>()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setHomeScreen(state: HomeState) {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(state = state, onIntent = { emittedIntents += it })
            }
        }
    }

    @Test
    fun `state의 계좌 목록이 화면에 표시된다`() {
        setHomeScreen(
            HomeState(
                accounts = listOf(
                    account(id = "acc-1", nickname = "월급통장"),
                    account(id = "acc-2", nickname = "비상금"),
                ),
            ),
        )

        composeRule.onNodeWithText("월급통장").assertIsDisplayed()
        composeRule.onNodeWithText("비상금").assertIsDisplayed()
    }

    @Test
    fun `isLoading이 true면 진행 인디케이터가 표시된다`() {
        setHomeScreen(HomeState(isLoading = true))

        composeRule.onNode(indeterminateProgress).assertIsDisplayed()
    }

    @Test
    fun `isLoading이 false면 진행 인디케이터가 없다`() {
        setHomeScreen(HomeState(isLoading = false))

        composeRule.onNode(indeterminateProgress).assertDoesNotExist()
    }

    @Test
    fun `새로고침 버튼을 누르면 Refresh 인텐트가 방출된다`() {
        setHomeScreen(HomeState())

        composeRule.onNodeWithText(string(R.string.home_action_refresh)).performClick()

        assertEquals(listOf(HomeIntent.Refresh), emittedIntents)
    }

    @Test
    fun `계좌 항목을 누르면 해당 accountId로 AccountClicked 인텐트가 방출된다`() {
        setHomeScreen(HomeState(accounts = listOf(account(id = "acc-1", nickname = "월급통장"))))

        composeRule.onNode(hasClickAction() and hasText("월급통장")).performClick()

        assertEquals(listOf(HomeIntent.AccountClicked("acc-1")), emittedIntents)
    }

    private fun account(id: String, nickname: String) = AccountUi(
        id = id,
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = nickname,
        balance = MoneyUi(BigDecimal("1000000"), CurrencyUi.KRW),
    )

    private companion object {
        val indeterminateProgress = SemanticsMatcher.expectValue(
            SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo.Indeterminate,
        )
    }
}
