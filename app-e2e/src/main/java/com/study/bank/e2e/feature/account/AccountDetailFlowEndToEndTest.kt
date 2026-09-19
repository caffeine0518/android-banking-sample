package com.study.bank.e2e.feature.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_BACK
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_SEND
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_TX_LABEL
import com.study.bank.core.ui.testing.BankTestTags.HOME_TOTAL_BALANCE
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_HOME
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.E2eAccounts
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AccountDetailFlowEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 계좌를_탭하면_상세_화면이_보인다() {
        // 이 테스트는 통화 무관 — 아무 계좌나 하나 열어 상세가 끝까지 표시되는지 검증한다(거래내역 적재는 페이징·비동기라 단정하지 않음).
        val account = E2eAccounts.firstOf(Currency.KRW)
        // 표시명이 아니라 안정적 id 태그로 그 계좌 행을 지목해 클릭.
        composeRule.awaitTag(accountItem(account))
        composeRule.onNodeWithTag(accountItem(account)).performClick()

        // 상세 헤더 태그는 계좌 로딩 후에만 등장 → 상세가 떴고 그 계좌가 로딩됐음을 보장한다.
        composeRule.awaitTag(accountDetail(account))
        composeRule.onNodeWithTag(DETAIL_TX_LABEL).assertIsDisplayed()
        // 하단 송금 진입 버튼.
        composeRule.onNodeWithTag(DETAIL_SEND).assertIsDisplayed()
    }

    @Test
    fun 상세에서_뒤로가기를_누르면_홈으로_돌아온다() {
        val account = E2eAccounts.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        composeRule.onNodeWithTag(accountItem(account)).performClick()
        composeRule.awaitTag(accountDetail(account))

        // 상단 백 버튼 → popBackStack → 홈.
        composeRule.onNodeWithTag(DETAIL_BACK).performClick()

        composeRule.awaitTag(SCREEN_HOME)                                 // 홈 도착
        composeRule.onNodeWithTag(HOME_TOTAL_BALANCE).assertIsDisplayed()
    }
}
