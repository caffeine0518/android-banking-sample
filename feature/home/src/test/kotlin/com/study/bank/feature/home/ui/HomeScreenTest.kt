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

/**
 * [HomeScreen]의 화면 단위 테스트.
 *
 * HomeScreen은 state를 받아 그리고 사용자 동작을 onIntent로만 내보내는 stateless composable이므로,
 * ViewModel 없이 state를 직접 주입해 (1) 렌더링과 (2) 동작→인텐트 매핑을 검증한다.
 * Robolectric으로 JVM(src/test)에서 구동 — 에뮬레이터 불필요.
 */
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** HomeScreen이 방출한 인텐트를 담아 두는 스파이. */
    private val intents = mutableListOf<HomeIntent>()

    private fun setHomeScreen(state: HomeState) {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    // ----- 렌더링 -----

    @Test
    fun `state의 계좌 목록이 화면에 표시된다`() {
        setHomeScreen(
            HomeState(
                accounts = listOf(account("acc-1", "월급통장"), account("acc-2", "비상금")),
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

    // ----- 동작 → 인텐트 -----

    @Test
    fun `새로고침 버튼을 누르면 Refresh 인텐트가 방출된다`() {
        setHomeScreen(HomeState())

        composeRule.onNodeWithText("새로고침").performClick()

        assertEquals(listOf(HomeIntent.Refresh), intents)
    }

    @Test
    fun `계좌 항목을 누르면 해당 accountId로 AccountClicked 인텐트가 방출된다`() {
        setHomeScreen(HomeState(accounts = listOf(account("acc-1", "월급통장"))))

        // Card의 clickable이 자식 텍스트를 머지하므로, 머지 노드 자신의 텍스트로 클릭 대상을 특정한다.
        composeRule.onNode(hasClickAction() and hasText("월급통장")).performClick()

        assertEquals(listOf(HomeIntent.AccountClicked("acc-1")), intents)
    }

    // ----- 픽스처 -----

    private fun account(id: String, nickname: String) = AccountUi(
        id = id,
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = nickname,
        balance = MoneyUi(BigDecimal("1000000"), CurrencyUi.KRW),
    )

    private companion object {
        /** Material3 무한(indeterminate) 진행 인디케이터의 시맨틱과 매칭. */
        val indeterminateProgress = SemanticsMatcher.expectValue(
            SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo.Indeterminate,
        )
    }
}
