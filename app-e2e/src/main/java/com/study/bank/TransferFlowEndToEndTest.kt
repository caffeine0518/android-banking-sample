package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.core.ui.testing.BankTestTags.AMOUNT_NEXT
import com.study.bank.core.ui.testing.BankTestTags.CONFIRM_SEND
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_SEND
import com.study.bank.core.ui.testing.BankTestTags.RESULT_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.RESULT_FAILURE
import com.study.bank.core.ui.testing.BankTestTags.RESULT_SUCCESS
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_AMOUNT
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_RECIPIENT
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
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
        openAmountScreen(recipientId = E2eSeedAccounts.SAFEBOX_KRW)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        // 확인 화면 도착(detail 로딩 후) → 보내기.
        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        // 로딩 → 성공 화면.
        composeRule.awaitTag(RESULT_SUCCESS)
    }

    @Test
    fun 통화가_다른_계좌로는_송금이_거절된다() {
        openAmountScreen(recipientId = E2eSeedAccounts.FOREIGN_USD)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        // KRW→USD는 mock 서버가 통화 불일치로 거절 → 성공이 아니라 실패 화면이 뜬다(문구는 검증 안 함).
        composeRule.awaitTag(RESULT_FAILURE)
    }

    @Test
    fun 송금_성공_후_확인하면_출금계좌_상세로_돌아간다() {
        openAmountScreen(recipientId = E2eSeedAccounts.SAFEBOX_KRW)
        enterDigits("10000")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        composeRule.awaitTag(RESULT_SUCCESS)

        // 보낸 뒤에는 시스템 뒤로가기가 차단된다(확인 화면으로 되돌아가 재송금 불가) — 여전히 성공 화면.
        pressSystemBack()
        composeRule.onNodeWithTag(RESULT_SUCCESS).assertIsDisplayed()

        // "확인" → 송금 플로우(수취인~결과)가 모두 걷히고 출금계좌(월급통장) 상세로 복귀.
        // 마스킹 번호 문자열이 아니라 출금계좌 id의 상세 태그로 "그 계좌 상세에 복귀"를 확인한다.
        composeRule.onNodeWithTag(RESULT_CONFIRM).performClick()
        composeRule.awaitTag(accountDetail(E2eSeedAccounts.PAYROLL_KRW))
        composeRule.onNodeWithTag(RESULT_SUCCESS).assertDoesNotExist()
    }

    @Test
    fun 같은_USD_계좌로_소수점_금액을_보내면_절삭_없이_송금된다() {
        // 출금=외화통장 USD($3,245.80), 수취=외화통장 USD 2. 동일 통화라 송금이 성립한다.
        openAmountScreen(
            recipientId = E2eSeedAccounts.FOREIGN_USD_2,
            sourceId = E2eSeedAccounts.FOREIGN_USD,
        )
        // $100.50 = 10,050센트를 키패드로 입력(소수점 키 없이 최소단위 누적).
        enterDigits("10050")

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()

        // 회귀 가드: 확인 화면에 정확히 $100.50이 떠야 한다. 이건 copy가 아니라 입력으로부터 계산된
        // 금액 값이라(문자열 리소스 무관) 텍스트로 그대로 단언한다 — 절삭/오해석 버그를 잡는 핵심.
        // (옛 절삭 코드는 잔액을 $3,245로 클램프하거나 10050을 $10,050로 오해석했다.)
        composeRule.awaitTag(SCREEN_CONFIRM)
        composeRule.onNodeWithText("$100.50", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(CONFIRM_SEND).performClick()

        composeRule.awaitTag(RESULT_SUCCESS)
    }

    /** 홈 → [sourceId] 상세 → 보내기 → 수취인 [recipientId] 선택 → 금액 화면 진입까지. 계좌는 모두 id 태그로 지목. */
    private fun openAmountScreen(
        recipientId: String,
        sourceId: String = E2eSeedAccounts.PAYROLL_KRW,
    ) {
        composeRule.awaitTag(accountItem(sourceId))
        composeRule.onNodeWithTag(accountItem(sourceId)).performClick()

        // 상세 헤더 태그 등장 = 계좌 로딩 완료(=보내기 버튼 활성) → 송금 진입.
        composeRule.awaitTag(accountDetail(sourceId))
        composeRule.onNodeWithTag(DETAIL_SEND).performClick()

        composeRule.awaitTag(SCREEN_RECIPIENT)                          // 수취인 화면 도착
        composeRule.onNodeWithTag(accountItem(recipientId)).performClick()

        composeRule.awaitTag(SCREEN_AMOUNT)                             // 금액 화면 도착
    }

    /** 커스텀 키패드로 [digits]를 한 자리씩 입력. 각 숫자 키는 화면에서 유일한 동일 텍스트 노드다. */
    private fun enterDigits(digits: String) {
        digits.forEach { composeRule.onNodeWithText(it.toString()).performClick() }
    }

    /** 시스템 뒤로가기(하드웨어 백/제스처)를 호출한다 — BackHandler 차단 여부를 검증하기 위함. */
    private fun pressSystemBack() {
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }
}
