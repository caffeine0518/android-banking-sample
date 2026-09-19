package com.study.bank.feature.home.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.study.bank.feature.home.contract.HomeState
import com.study.bank.feature.home.ui.preview.PreviewHomeState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [HomeScreen]의 골든 스크린샷 테스트 — 색·간격·레이아웃 회귀를 실제 픽셀로 검출한다.
 * 골든은 src/test/screenshots에 저장된다.
 *
 * - 골든 기록: ./gradlew :feature:home:recordRoborazziDebug
 * - 회귀 검증: ./gradlew :feature:home:verifyRoborazziDebug
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
class HomeScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `계좌가 채워진 홈 화면`() {
        captureHomeScreen("HomeScreen_populated", PreviewHomeState)
    }

    @Test
    fun `로딩 중 홈 화면`() {
        captureHomeScreen("HomeScreen_loading", HomeState(isLoading = true))
    }

    @Test
    fun `빈 홈 화면`() {
        captureHomeScreen("HomeScreen_empty", HomeState())
    }

    private fun captureHomeScreen(name: String, state: HomeState) {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
