package com.study.bank

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.data.di.NetworkFaultController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * "새로고침 실패 → 에러 스낵바" 경로를 검증하는 E2E.
 *
 * 해피패스([HomeFlowEndToEndTest])와 달리 이 테스트는 **실 DI 그래프의 [NetworkFaultController]를
 * 테스트가 직접 잡아(@Inject) 서버를 장애 상태로 전환**한다 — Hilt 계측 테스트가 비로소 값을 하는 지점.
 * 이 seam은 mock 서버 싱글톤을 감싸므로, 토글 대상이 곧 AccountRepository가 호출하는 그 서버다.
 * (덕분에 :app은 mock 구현 모듈 :data:remote:kftc를 직접 의존하지 않는다.)
 *
 * 시나리오: 부팅 시 자동 새로고침은 성공(시드 표시) → 서버 장애로 전환 → 수동 새로고침이 5xx로 실패 →
 * ShowRefreshError가 스낵바로 노출 + 직전 성공 데이터는 유지(refresh 실패 시 dao.replaceAll 미호출).
 *
 * 에뮬레이터/디바이스에서 실행: ./gradlew :app:connectedDebugAndroidTest
 */
@HiltAndroidTest
class HomeRefreshErrorEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    // 실 그래프가 쓰는 mock 서버를 감싼 seam. 이걸로 응답을 테스트가 제어한다.
    @Inject
    lateinit var faultController: NetworkFaultController

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun 새로고침이_실패하면_에러_스낵바가_뜨고_기존_계좌는_유지된다() {
        // 앱 부팅 시 자동 Refresh가 성공해 시드 계좌가 뜰 때까지 대기.
        composeRule.awaitText("월급통장")
        // isLoading=true면 새로고침 인텐트가 무시되므로, 초기 로딩이 끝난 뒤 클릭한다.
        composeRule.awaitNotLoading()

        // 서버를 장애로 전환 → 다음 새로고침의 list_finuse가 5xx로 실패한다.
        faultController.enableFault()
        composeRule.onNodeWithText("새로고침").performClick()

        // 실패가 ShowRefreshError → 에러 스낵바로 노출(블랙박스: 실제 카피를 그대로 단언).
        composeRule.awaitText("계좌를 새로고침하지 못했어요. 잠시 후 다시 시도해 주세요.")
        // refresh 실패 시 dao.replaceAll을 타지 않으므로 직전 성공 데이터는 유지된다.
        composeRule.onNodeWithText("월급통장").assertIsDisplayed()
    }
}
