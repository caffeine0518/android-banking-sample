package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * 송금 풀 플로우 E2E: 홈 → 계좌 상세 → 수취인 → 금액 → 확인 → 결과.
 *
 * 화면별로 끊어 보는 단일 화면 테스트(RecipientScreenTest/AmountScreenTest/…)와 달리, 실제
 * MainActivity → BankNavHost → 각 화면 hiltViewModel → AccountRepository/ExecuteTransferUseCase →
 * KFTC MockWebServer까지 송금 한 건을 끝에서 끝까지 실행한다(실 DI·실 네비게이션·실 HTTP).
 *
 * 출금계좌는 시드 KRW '월급통장'. 같은 통화(KRW) 내 계좌로 보내면 성공하고, 다른 통화(USD) 계좌로
 * 보내면 mock 서버가 통화 불일치로 업무 거절(A0001)해 실패 화면이 뜬다 — 다통화 거절 경로까지 검증.
 *
 * 비-시작 목적지를 여럿 거치므로, 파괴 시 NavBackStackEntry 전이 크래시를 막는 [TestDispatchersModule]이
 * 필수다(MviStore reducer를 메인에 묶어 전환이 settle되게 함 — 자세한 사유는 그 KDoc 참고).
 *
 * 에뮬레이터/디바이스에서 실행: ./gradlew :app-e2e:connectedDebugAndroidTest
 */
@HiltAndroidTest
class TransferFlowEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 같은_통화_내_계좌로_송금하면_성공_화면이_보인다() {
        openAmountScreen(recipientLabel = "세이프박스")
        enterAmount10000()

        composeRule.awaitText("다음")
        composeRule.onNodeWithText("다음").performClick()

        // 확인 화면 제목(detail 로딩 후 노출) → 보내기.
        composeRule.awaitText("보낼까요?")
        composeRule.onNodeWithText("보내기").performClick()

        // 로딩("보내는 중이에요") → 성공 화면.
        composeRule.awaitText("보냈어요")
    }

    @Test
    fun 통화가_다른_계좌로는_송금이_거절된다() {
        openAmountScreen(recipientLabel = "외화통장 USD")
        enterAmount10000()

        composeRule.awaitText("다음")
        composeRule.onNodeWithText("다음").performClick()

        composeRule.awaitText("보낼까요?")
        composeRule.onNodeWithText("보내기").performClick()

        // KRW→USD는 mock 서버가 통화 불일치로 거절 → 실패 화면 + 사유 메시지.
        composeRule.awaitText("보내지 못했어요")
        composeRule.onNodeWithText("다른 통화 계좌로는 보낼 수 없어요").assertIsDisplayed()
    }

    /** 홈 → '월급통장' 상세 → 보내기 → 수취인 [recipientLabel] 선택 → 금액 화면 진입까지. */
    private fun openAmountScreen(recipientLabel: String) {
        composeRule.awaitText("월급통장")
        composeRule.onNodeWithText("월급통장").performClick()

        // 상세 헤더 마스킹 번호로 계좌 로딩(=보내기 버튼 활성)을 보장한 뒤 송금 진입.
        composeRule.awaitText("1000-12-***6789")
        composeRule.onNodeWithText("보내기").performClick()

        composeRule.awaitText("어디로 돈을 보낼까요?")
        composeRule.onNodeWithText(recipientLabel).performClick()

        composeRule.awaitText("얼마나 옮길까요?")
    }

    /** 커스텀 키패드로 10,000 입력(1 → 0 0 0 0). "0"/"1" 키는 화면에서 유일한 동일 텍스트 노드다. */
    private fun enterAmount10000() {
        composeRule.onNodeWithText("1").performClick()
        repeat(4) { composeRule.onNodeWithText("0").performClick() }
    }
}
