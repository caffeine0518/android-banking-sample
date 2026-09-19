package com.study.bank.e2e.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.HOME_REFRESH
import com.study.bank.core.ui.testing.BankTestTags.HOME_TOTAL_BALANCE
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_HOME
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.E2eAccounts
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

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
        // 표시명이 아니라 안정적 id 태그로 "그 계좌 행이 렌더됐는지"를 확인한다.
        val krwAccount = E2eAccounts.firstOf(Currency.KRW)
        val usdAccount = E2eAccounts.firstOf(Currency.USD)
        composeRule.awaitTag(accountItem(krwAccount))
        composeRule.onNodeWithTag(SCREEN_HOME).assertIsDisplayed()
        composeRule.onNodeWithTag(HOME_TOTAL_BALANCE).assertIsDisplayed()

        composeRule.onNodeWithTag(accountItem(krwAccount)).assertIsDisplayed() // KRW 시드
        composeRule.onNodeWithTag(accountItem(usdAccount)).assertIsDisplayed() // 다통화(USD) 시드
    }

    @Test
    fun 새로고침을_누르면_갱신_후에도_계좌_목록이_유지된다() {
        val account = E2eAccounts.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))

        composeRule.onNodeWithTag(HOME_REFRESH).performClick()

        composeRule.awaitTag(accountItem(account)) // 갱신 후에도 계좌 목록 유지
        composeRule.onNodeWithTag(accountItem(account)).assertIsDisplayed()
    }
}
