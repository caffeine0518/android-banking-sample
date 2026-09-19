package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_BACK
import com.study.bank.core.ui.testing.BankTestTags.HOME_TOTAL_BALANCE
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_HOME
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.domain.model.Currency
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * 더블탭 내비게이션 가드(BankNavHost의 push/pop 헬퍼) 회귀 E2E.
 *
 * 클릭→intent→effect→콜백의 비동기 홉 때문에 리컴포지션 반영 전 더블탭이 백스택을 두 번
 * 조작할 수 있다. 가드 도입 전 실기 재현된 증상:
 * - 전진 더블탭 → 동일 NavKey 중복 push(contentKey 공유로 상태·VM 섞임, 뒤로가기 2번 필요)
 * - 백버튼 더블탭 → 백스택 소진 → "NavDisplay backstack cannot be empty" 크래시
 *
 * [performTouchInput] 한 배치에 클릭 2회를 넣어야 재현된다 — performClick 2회는 사이에 idle 동기화가 낀다.
 */
@HiltAndroidTest
class NavigationDoubleTapGuardEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 계좌를_더블탭해도_상세는_한_번만_쌓인다() {
        val account = E2eAccounts.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))

        composeRule.onNodeWithTag(accountItem(account)).performTouchInput {
            click(center)
            advanceEventTime(DOUBLE_TAP_GAP_MILLIS)
            click(center)
        }
        composeRule.awaitTag(accountDetail(account))

        // 중복 push였다면 백 1번 뒤에도 상세에 남는다 — 가드가 있으면 곧장 홈.
        composeRule.onNodeWithTag(DETAIL_BACK).performClick()
        composeRule.awaitTag(SCREEN_HOME)
        composeRule.onNodeWithTag(HOME_TOTAL_BALANCE).assertIsDisplayed()
    }

    @Test
    fun 상세_백버튼을_더블탭해도_크래시_없이_홈에_머문다() {
        val account = E2eAccounts.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        composeRule.onNodeWithTag(accountItem(account)).performClick()
        composeRule.awaitTag(accountDetail(account))

        // 가드 도입 전에는 두 번째 pop이 백스택을 비워 NavDisplay가 즉시 죽었다.
        composeRule.onNodeWithTag(DETAIL_BACK).performTouchInput {
            click(center)
            advanceEventTime(DOUBLE_TAP_GAP_MILLIS)
            click(center)
        }

        composeRule.awaitTag(SCREEN_HOME)
        composeRule.onNodeWithTag(HOME_TOTAL_BALANCE).assertIsDisplayed()
    }

    private companion object {
        /** 첫 탭의 내비 전환이 시작된 뒤, 이전 화면이 아직 살아 있는 사이에 두 번째 탭이 꽂히는 간격. */
        const val DOUBLE_TAP_GAP_MILLIS = 120L
    }
}
