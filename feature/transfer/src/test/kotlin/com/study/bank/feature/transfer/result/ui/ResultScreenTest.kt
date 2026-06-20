package com.study.bank.feature.transfer.result.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.transfer.R
import com.study.bank.feature.transfer.result.contract.ResultIntent
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.contract.ResultState
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import com.study.bank.feature.transfer.result.ui.model.ResultHeaderUi
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ResultScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<ResultIntent>()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setScreen(state: ResultState) {
        composeRule.setContent {
            MaterialTheme {
                ResultScreen(state = state, onIntent = { intents += it })
            }
        }
    }

    @Test
    fun `로딩 상태는 안내 문구를 보이고 하단 버튼은 없다`() {
        setScreen(ResultState(header = null, phase = ResultPhase.Loading))

        composeRule.onNodeWithText(string(R.string.transfer_result_loading)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_confirm)).assertDoesNotExist()
    }

    @Test
    fun `성공 상태는 제목·메모칩·공유하기·확인을 보인다`() {
        setScreen(ResultState(header = header(), phase = ResultPhase.Success))

        composeRule.onNodeWithText("안성재", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_success_sent)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_leave_memo)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_share)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_confirm)).assertIsDisplayed()
    }

    @Test
    fun `실패 상태는 실패 제목·사유·다시 시도를 보인다`() {
        setScreen(
            ResultState(
                header = header(),
                phase = ResultPhase.Failure(ResultFailureUi.INSUFFICIENT_FUNDS),
            ),
        )

        composeRule.onNodeWithText(string(R.string.transfer_result_failure_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_error_insufficient)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_retry)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.transfer_result_share)).assertDoesNotExist()
    }

    @Test
    fun `확인을 누르면 ConfirmClicked 인텐트가 방출된다`() {
        setScreen(ResultState(header = header(), phase = ResultPhase.Success))

        composeRule.onNodeWithText(string(R.string.transfer_result_confirm)).performClick()

        assertEquals(listOf(ResultIntent.ConfirmClicked), intents)
    }

    @Test
    fun `실패 상태에서 다시 시도를 누르면 RetryClicked 인텐트가 방출된다`() {
        setScreen(
            ResultState(
                header = header(),
                phase = ResultPhase.Failure(ResultFailureUi.NETWORK),
            ),
        )

        composeRule.onNodeWithText(string(R.string.transfer_result_retry)).performClick()

        assertEquals(listOf(ResultIntent.RetryClicked), intents)
    }

    private fun header() = ResultHeaderUi(
        recipientName = "안성재",
        amount = MoneyUi(BigDecimal.ONE, CurrencyUi.KRW),
    )
}
