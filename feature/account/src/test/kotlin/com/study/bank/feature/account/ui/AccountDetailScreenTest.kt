package com.study.bank.feature.account.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.study.bank.core.ui.model.CurrencyUi
import com.study.bank.core.ui.model.MoneyUi
import com.study.bank.feature.account.R
import com.study.bank.feature.account.contract.AccountDetailIntent
import com.study.bank.feature.account.contract.AccountDetailState
import com.study.bank.feature.account.ui.model.AccountTypeUi
import com.study.bank.feature.account.ui.model.AccountUi
import com.study.bank.feature.account.ui.model.TransactionTypeUi
import com.study.bank.feature.account.ui.model.TransactionUi
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * [AccountDetailScreen]의 화면 단위 테스트. state를 직접 주입해 (1) 렌더링과 (2) 동작→인텐트 매핑을 검증한다.
 * Robolectric으로 JVM(src/test)에서 구동 — 에뮬레이터 불필요.
 */
@RunWith(RobolectricTestRunner::class)
class AccountDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<AccountDetailIntent>()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    // 적재 완료(NotLoading) 상태로 PagingData를 만든다 — 안 주면 refresh가 Loading으로 남아 '빈 안내' 조건이 안 켜진다.
    private val idleLoadStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    private fun txFlow(items: List<TransactionUi>): Flow<PagingData<TransactionUi>> =
        flowOf(PagingData.from(items, sourceLoadStates = idleLoadStates))

    private fun setScreen(
        state: AccountDetailState,
        transactions: Flow<PagingData<TransactionUi>> = txFlow(emptyList()),
    ) {
        composeRule.setContent {
            MaterialTheme {
                AccountDetailScreen(
                    state = state,
                    transactions = transactions,
                    onIntent = { intents += it },
                )
            }
        }
    }

    // ----- 렌더링 -----

    @Test
    fun `계좌 닉네임과 거래 상대가 화면에 표시된다`() {
        setScreen(
            state = AccountDetailState(account = account()),
            transactions = txFlow(listOf(transaction("tx-1", "세이프박스"))),
        )

        composeRule.onNodeWithText("월급통장").assertIsDisplayed()
        composeRule.onNodeWithText("세이프박스").assertIsDisplayed()
    }

    @Test
    fun `거래내역이 없으면 빈 안내가 표시된다`() {
        setScreen(
            state = AccountDetailState(account = account()),
            transactions = txFlow(emptyList()),
        )

        composeRule.onNodeWithText(string(R.string.account_transactions_empty)).assertIsDisplayed()
    }

    // ----- 동작 → 인텐트 -----

    @Test
    fun `보내기 버튼을 누르면 SendClicked 인텐트가 방출된다`() {
        setScreen(AccountDetailState(account = account()))

        composeRule.onNode(hasClickAction() and hasText(string(R.string.account_action_send))).performClick()

        assertEquals(listOf(AccountDetailIntent.SendClicked), intents)
    }

    @Test
    fun `새로고침을 누르면 Refresh 인텐트가 방출된다`() {
        setScreen(AccountDetailState(account = account()))

        composeRule.onNodeWithText(string(R.string.account_action_refresh)).performClick()

        assertEquals(listOf(AccountDetailIntent.Refresh), intents)
    }

    @Test
    fun `백 버튼을 누르면 BackClicked 인텐트가 방출된다`() {
        setScreen(AccountDetailState(account = account()))

        // 아이콘 버튼이라 텍스트가 아닌 contentDescription으로 특정한다.
        composeRule.onNodeWithContentDescription(string(R.string.account_action_back)).performClick()

        assertEquals(listOf(AccountDetailIntent.BackClicked), intents)
    }

    // ----- 픽스처 -----

    private fun account() = AccountUi(
        id = "acc-1",
        bankDisplayName = "토스뱅크",
        type = AccountTypeUi.CHECKING,
        nickname = "월급통장",
        numberMasked = "1000-12-***6789",
        balance = MoneyUi(BigDecimal("2797320"), CurrencyUi.KRW),
    )

    private fun transaction(id: String, counterparty: String) = TransactionUi(
        id = id,
        type = TransactionTypeUi.TRANSFER_OUT,
        counterpartyName = counterparty,
        amount = MoneyUi(BigDecimal("50000"), CurrencyUi.KRW),
        occurredAtLabel = "2026.06.18",
    )
}
