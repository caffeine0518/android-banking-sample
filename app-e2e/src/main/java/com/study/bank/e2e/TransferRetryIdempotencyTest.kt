package com.study.bank.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.study.bank.MainActivity
import com.study.bank.core.ui.testing.BankTestTags.AMOUNT_NEXT
import com.study.bank.core.ui.testing.BankTestTags.CONFIRM_SEND
import com.study.bank.core.ui.testing.BankTestTags.DETAIL_SEND
import com.study.bank.core.ui.testing.BankTestTags.RESULT_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.RESULT_FAILURE
import com.study.bank.core.ui.testing.BankTestTags.RESULT_RETRY
import com.study.bank.core.ui.testing.BankTestTags.RESULT_SUCCESS
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_AMOUNT
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_CONFIRM
import com.study.bank.core.ui.testing.BankTestTags.SCREEN_RECIPIENT
import com.study.bank.core.ui.testing.BankTestTags.accountDetail
import com.study.bank.core.ui.testing.BankTestTags.accountItem
import com.study.bank.data.remote.kftc.mock.KftcMockServer
import com.study.bank.domain.model.Currency
import com.study.bank.e2e.support.AccountsByCurrency
import com.study.bank.e2e.support.awaitTag
import com.study.bank.e2e.support.withNetworkDown
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * "응답 유실 → 재시도"가 이중출금이 되지 않는지 검증하는 E2E.
 *
 * 송금 요청은 서버에 도달해 원장에 반영됐는데 응답만 유실되는 상황을 만든다. 이때 결과 화면은 실패를
 * 표시하고, 사용자가 재시도하면 같은 멱등성 키로 다시 나간다. 서버가 그 키로 중복을 판정해야
 * 잔액이 한 번만 차감된다 — 판정하지 못하면 두 번 차감된다.
 */
@HiltAndroidTest
class TransferRetryIdempotencyTest {

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
    fun 응답이_유실된_뒤_재시도해도_잔액은_한_번만_차감된다() {
        val (source, recipient) = AccountsByCurrency.sameCurrencyPair(Currency.KRW)
        openAmountScreen(sourceId = source, recipientId = recipient)
        enterDigits(SEND_AMOUNT_DIGITS)

        composeRule.awaitTag(AMOUNT_NEXT)
        composeRule.onNodeWithTag(AMOUNT_NEXT).performClick()
        composeRule.awaitTag(SCREEN_CONFIRM)

        // 출금은 서버 원장에 반영되고 응답만 유실된다 → 앱은 전송 실패로 판단해 실패 화면을 띄운다.
        mockServer.withNetworkDown {
            composeRule.onNodeWithTag(CONFIRM_SEND).performClick()
            composeRule.awaitTag(RESULT_FAILURE)
        }

        // 복구 후 재시도. 같은 송금이므로 멱등성 키가 유지돼 서버가 중복으로 판정해야 한다.
        composeRule.onNodeWithTag(RESULT_RETRY).performClick()
        composeRule.awaitTag(RESULT_SUCCESS)

        // 출금계좌 상세로 복귀해 잔액을 확인한다. 카피가 아니라 입력으로부터 계산된 값이라 텍스트로 단언한다.
        composeRule.onNodeWithTag(RESULT_CONFIRM).performClick()
        composeRule.awaitTag(accountDetail(source))
        composeRule.onNodeWithText(BALANCE_AFTER_ONE_SEND, substring = true).assertIsDisplayed()
    }

    /** 홈 → [sourceId] 상세 → 보내기 → 수취인 [recipientId] 선택 → 금액 화면 진입까지. 계좌는 모두 id 태그로 지목. */
    private fun openAmountScreen(sourceId: String, recipientId: String) {
        composeRule.awaitTag(accountItem(sourceId))
        composeRule.onNodeWithTag(accountItem(sourceId)).performClick()

        composeRule.awaitTag(accountDetail(sourceId))
        composeRule.onNodeWithTag(DETAIL_SEND).performClick()

        composeRule.awaitTag(SCREEN_RECIPIENT)
        composeRule.onNodeWithTag(accountItem(recipientId)).performClick()

        composeRule.awaitTag(SCREEN_AMOUNT)
    }

    /** 커스텀 키패드로 [digits]를 한 자리씩 입력. 각 숫자 키는 화면에서 유일한 동일 텍스트 노드다. */
    private fun enterDigits(digits: String) {
        digits.forEach { composeRule.onNodeWithText(it.toString()).performClick() }
    }

    private companion object {
        const val SEND_AMOUNT_DIGITS = "10000"
        // KRW 첫 시드 계좌 2,847,320원에서 10,000원을 1회 차감.
        // 중복 판정이 없으면 2,817,320원이 된다 — OkHttp가 연결 실패를 자동 재전송해 2회,
        // 사용자의 재시도로 1회, 합쳐 3회 차감되기 때문이다.
        const val BALANCE_AFTER_ONE_SEND = "2,837,320"
    }
}
