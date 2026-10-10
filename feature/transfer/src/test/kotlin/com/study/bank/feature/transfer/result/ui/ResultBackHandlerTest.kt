package com.study.bank.feature.transfer.result.ui

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.study.bank.feature.transfer.result.contract.ResultPhase
import com.study.bank.feature.transfer.result.ui.model.ResultFailureUi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResultBackHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var phase by mutableStateOf<ResultPhase>(ResultPhase.Loading)
    private var reachedPreviousScreen = false

    @Before
    fun setUp() {
        // 먼저 등록된 콜백이 이전 화면이다. 결과 화면이 소비하지 않은 뒤로가기만 도달한다.
        val previousScreen = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                reachedPreviousScreen = true
            }
        }
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.addCallback(previousScreen) }
        composeRule.setContent { ResultBackHandler(phase = phase) }
    }

    @Test
    fun `서버 반영 여부를 알 수 없는 실패에서는 뒤로가기가 차단된다`() {
        listOf(ResultFailureUi.NETWORK, ResultFailureUi.UNKNOWN).forEach { reason ->
            assertEquals(
                "$reason 실패에서 뒤로가기가 이전 화면으로 전달됐다",
                false,
                backReachesPreviousScreen(ResultPhase.Failure(reason)),
            )
        }
    }

    @Test
    fun `출금되지 않은 것이 확정된 실패에서는 뒤로가기가 허용된다`() {
        listOf(
            ResultFailureUi.INSUFFICIENT_FUNDS,
            ResultFailureUi.INVALID_RECIPIENT,
            ResultFailureUi.CURRENCY_MISMATCH,
            ResultFailureUi.LIMIT_EXCEEDED,
        ).forEach { reason ->
            assertEquals(
                "$reason 실패에서 뒤로가기가 차단됐다",
                true,
                backReachesPreviousScreen(ResultPhase.Failure(reason)),
            )
        }
    }

    @Test
    fun `로딩·성공에서는 뒤로가기가 차단된다`() {
        assertEquals(false, backReachesPreviousScreen(ResultPhase.Loading))
        assertEquals(false, backReachesPreviousScreen(ResultPhase.Success))
    }

    private fun backReachesPreviousScreen(phase: ResultPhase): Boolean {
        this.phase = phase
        reachedPreviousScreen = false
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        return reachedPreviousScreen
    }
}
