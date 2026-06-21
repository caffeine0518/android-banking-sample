package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * "홈에서 계좌 탭 → 계좌 상세 → 뒤로 홈" 화면 전환을 검증하는 E2E.
 *
 * 홈 한 화면에 머무는 [HomeFlowEndToEndTest]와 달리 실제 네비게이션(BankNavHost)을 타고
 * HomeRoute → AccountDetailRoute로 이동해 상세 헤더(마스킹 번호·잔액)와 거래내역(시드 직후 비어 있음)이
 * 끝까지 흐르는지 본다. 상단 백 버튼으로 홈에 복귀하는 popBackStack 경로까지 함께 확인한다.
 *
 * 네비게이션 후 파괴 시 NavBackStackEntry 전이 크래시를 피하려면 [TestDispatchersModule]이 필수다
 * (MviStore reducer를 메인 디스패처에 묶어 진입 전환이 settle되게 함 — 자세한 사유는 그 KDoc 참고).
 *
 * 에뮬레이터/디바이스에서 실행: ./gradlew :app-e2e:connectedDebugAndroidTest
 */
@HiltAndroidTest
class AccountDetailFlowEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 계좌를_탭하면_상세_화면과_빈_거래내역이_보인다() {
        composeRule.awaitText("대만 여행자금")
        composeRule.onNodeWithText("대만 여행자금").performClick()

        // 상세 헤더의 마스킹 번호는 이 화면에만 있어, 상세가 떴고 계좌가 로딩됐음을 보장한다.
        composeRule.awaitText("1000-77-***3322")
        composeRule.onNodeWithText("거래 내역").assertIsDisplayed()
        // 시드 직후 원장은 비어 있으므로 빈 거래내역 안내가 보인다.
        composeRule.onNodeWithText("거래 내역이 없어요").assertIsDisplayed()
        // 하단 송금 진입 버튼.
        composeRule.onNodeWithText("보내기").assertIsDisplayed()
    }

    @Test
    fun 상세에서_뒤로가기를_누르면_홈으로_돌아온다() {
        composeRule.awaitText("대만 여행자금")
        composeRule.onNodeWithText("대만 여행자금").performClick()
        composeRule.awaitText("1000-77-***3322")

        // 상단 백 버튼(contentDescription="뒤로") → popBackStack → 홈.
        composeRule.onNodeWithContentDescription("뒤로").performClick()

        composeRule.awaitText("내 계좌")
        composeRule.onNodeWithText("총 자산").assertIsDisplayed()
    }
}
