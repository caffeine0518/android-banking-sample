package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.study.bank.feature.home.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * QA의 매뉴얼 테스트를 흉내 내는 퍼스트 파티 E2E(end-to-end).
 *
 * 단일 화면을 격리하는 [com.study.bank.feature.home.ui.HomeScreenTest]와 달리, 실제 [MainActivity] →
 * BankNavHost → HomeRoute → hiltViewModel() → AccountRepository → KFTC MockWebServer까지 **앱 전체를
 * 진짜로 부팅**해, 시드된 계좌가 끝까지 흐르는 사용자 여정을 검증한다(실 DI·실 네비게이션·실 HTTP).
 *
 * Hilt 그래프를 그대로 쓰므로(@TestInstallIn 교체 없음) 화면에 뜨는 데이터는 KftcAccountSeed가 정답.
 * KftcMockServer가 메인 스레드 안전하게 바뀌어, 컴포지션 중 네트워크 스택이 생성돼도 안전하다(별도 warmup 불필요).
 *
 * 에뮬레이터/디바이스에서 실행: ./gradlew :app:connectedDebugAndroidTest
 */
@HiltAndroidTest
class HomeFlowEndToEndTest {

    // order=0: Activity가 뜨기 전에 Hilt 컴포넌트를 먼저 구성해야 한다.
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 앱을_켜면_시드된_계좌_목록과_총자산이_보인다() {
        // 네트워크(MockWebServer) 라운드트립 후 계좌 스트림이 도착할 때까지 대기.
        composeRule.awaitText("월급통장")
        composeRule.onNodeWithText("내 계좌").assertIsDisplayed()
        composeRule.onNodeWithText("총 자산").assertIsDisplayed()

        composeRule.onNodeWithText("월급통장").assertIsDisplayed()      // 토스뱅크 KRW 시드
        composeRule.onNodeWithText("외화통장 USD").assertIsDisplayed()  // 다통화 시드
    }

    @Test
    fun 새로고침을_누르면_갱신_후에도_계좌_목록이_유지된다() {
        composeRule.awaitText("월급통장")

        composeRule.onNodeWithText("새로고침").performClick()

        composeRule.awaitText("월급통장")
        composeRule.onNodeWithText("세이프박스").assertIsDisplayed()
    }
}

/** 비동기 로드(네트워크/DB)를 기다렸다가 해당 텍스트 노드가 나타날 때까지 폴링. */
private fun ComposeContentTestRule.awaitText(text: String, timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
