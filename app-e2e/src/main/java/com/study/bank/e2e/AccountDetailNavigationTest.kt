package com.study.bank.e2e

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
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AccountDetailNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 계좌를_탭하면_상세_화면이_보인다() {
        // 거래내역 적재는 페이징·비동기라 단언하지 않는다.
        val account = AccountsByCurrency.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        composeRule.onNodeWithTag(accountItem(account)).performClick()

        composeRule.awaitTag(accountDetail(account))
        composeRule.onNodeWithTag(DETAIL_TX_LABEL).assertIsDisplayed()
        composeRule.onNodeWithTag(DETAIL_SEND).assertIsDisplayed()
    }

    @Test
    fun 상세에서_뒤로가기를_누르면_홈으로_돌아온다() {
        val account = AccountsByCurrency.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        composeRule.onNodeWithTag(accountItem(account)).performClick()
        composeRule.awaitTag(accountDetail(account))

        composeRule.onNodeWithTag(DETAIL_BACK).performClick()

        composeRule.awaitTag(SCREEN_HOME)
        composeRule.onNodeWithTag(HOME_TOTAL_BALANCE).assertIsDisplayed()
    }
}
