package com.study.bank.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.HOME_REFRESH
import com.study.bank.core.ui.testing.BankTestTags.HOME_SNACKBAR
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitNotLoading
import com.study.bank.e2e.support.awaitTag
import com.study.bank.e2e.support.withNetworkDown
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** 스텁 응답이 아니라 앱이 호출하는 mock 서버의 연결을 차단해 전송 실패를 만든다. */
@HiltAndroidTest
class HomeRefreshFailureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var mockServer: KftcMockServer

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun 새로고침이_실패하면_에러_스낵바가_뜨고_기존_계좌는_유지된다() {
        val account = AccountsByCurrency.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        composeRule.awaitNotLoading()

        mockServer.withNetworkDown {
            composeRule.onNodeWithTag(HOME_REFRESH).performClick()
            composeRule.awaitTag(HOME_SNACKBAR)
        }

        // refresh 실패 시 dao.replaceAll을 타지 않으므로 직전 성공 데이터(그 계좌 행)는 유지된다.
        composeRule.onNodeWithTag(accountItem(account)).assertIsDisplayed()
    }
}
