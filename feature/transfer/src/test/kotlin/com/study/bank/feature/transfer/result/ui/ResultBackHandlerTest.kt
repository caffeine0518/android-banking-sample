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

/**
 * 결과 화면의 시스템 뒤로가기 허용 여부.
 *
 * 뒤로가기로 결과 화면을 벗어나면 ResultViewModel 과 멱등성 키가 함께 제거되고, 금액·확인 화면을 거쳐
 * 다시 보내면 새 키가 발급된다. 서버 반영 여부를 알 수 없는 실패(NETWORK·UNKNOWN)에서 이 경로가 열려
 * 있으면, 응답만 유실된 첫 송금과 새 키의 재송금이 모두 체결되어 이중출금이 발생한다.
 */
@RunWith(RobolectricTestRunner::class)
class ResultBackHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var phase by mutableStateOf<ResultPhase>(ResultPhase.Loading)
    private var reachedPreviousScreen = false

    @Before
    fun setUp() {
        // 결과 화면보다 먼저 등록된 콜백 = 백스택의 이전 화면. 결과 화면이 소비하지 않은 뒤로가기만 여기에 도달한다.
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

    /** [phase]에서 뒤로가기를 누르면 결과 화면이 소비하지 않고 이전 화면까지 전달되는지 반환한다. */
    private fun backReachesPreviousScreen(phase: ResultPhase): Boolean {
        this.phase = phase
        reachedPreviousScreen = false
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        return reachedPreviousScreen
    }
}
