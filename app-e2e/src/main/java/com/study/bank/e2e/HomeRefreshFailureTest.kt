package com.study.bank.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.HOME_REFRESH
import com.study.bank.core.ui.testing.BankTestTags.HOME_SNACKBAR
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.data.di.kftc.NetworkFaultController
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitNotLoading
import com.study.bank.e2e.support.awaitTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * "새로고침 실패 → 에러 스낵바" 경로를 검증하는 E2E.
 *
 * **실 DI 그래프의 [NetworkFaultController]를 테스트가 직접 주입받아 서버를 장애 상태로 전환**한다.
 * 이 seam이 감싸는 대상이 곧 AccountRepository가 호출하는 mock 싱글톤이고, 덕분에 :app은 구현 모듈
 * :data:remote:kftc를 직접 의존하지 않는다.
 */
@HiltAndroidTest
class HomeRefreshFailureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var faultController: NetworkFaultController

    @Before
    fun inject() {
        hiltRule.inject()
    }

    // mock 서버는 프로세스 전역 @Singleton이라 활성화한 장애가 다음 테스트까지 남는다. 명시적으로 정상 복구한다.
    @After
    fun clearFault() {
        faultController.disableFault()
    }

    @Test
    fun 새로고침이_실패하면_에러_스낵바가_뜨고_기존_계좌는_유지된다() {
        // 앱 부팅 시 자동 Refresh가 성공해 시드 계좌가 뜰 때까지 대기(표시명이 아닌 id 태그로).
        val account = AccountsByCurrency.firstOf(Currency.KRW)
        composeRule.awaitTag(accountItem(account))
        // isLoading=true면 새로고침 인텐트가 무시되므로, 초기 로딩이 끝난 뒤 클릭한다.
        composeRule.awaitNotLoading()

        // 서버를 장애로 전환 → 다음 새로고침의 list_finuse가 5xx로 실패한다.
        faultController.enableFault()
        composeRule.onNodeWithTag(HOME_REFRESH).performClick()

        // 실패가 ShowRefreshError → 에러 스낵바 노출. 문구가 아니라 "스낵바가 떴다"는 사실만 태그로 확인.
        composeRule.awaitTag(HOME_SNACKBAR)
        // refresh 실패 시 dao.replaceAll을 타지 않으므로 직전 성공 데이터(그 계좌 행)는 유지된다.
        composeRule.onNodeWithTag(accountItem(account)).assertIsDisplayed()
    }
}
