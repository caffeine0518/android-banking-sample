package com.study.bank

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag

/**
 * E2E에서 비동기 로드(네트워크/DB)가 끝나길 기다리는 폴링 헬퍼 모음.
 *
 * 여러 E2E가 공유하므로 한곳에 모은다.
 */

/**
 * 해당 testTag 노드가 나타날 때까지 폴링.
 *
 * 서버 표시값(계좌명·잔액)이나 앱 카피(버튼·제목 문구)에 의존하지 않고, 동적 항목·정적 화면/컨트롤을
 * 모두 [com.study.bank.core.ui.testing.BankTestTags] 태그로 식별한다 — 문자열 리소싱 전략이 바뀌어도
 * 안 깨진다. 로드가 끝나야 등장하는 노드(예: 계좌 상세 헤더)의 태그 등장은 곧 "로딩 완료 + 도착"이다.
 */
internal fun ComposeContentTestRule.awaitTag(tag: String, timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

/**
 * 무한 진행 인디케이터가 사라질 때까지(= 로딩 종료) 폴링.
 *
 * HomeViewModel은 isLoading=true면 새로고침 인텐트를 무시하므로, 초기 로딩이 끝난 뒤 클릭해야
 * 클릭이 유실되지 않는다.
 */
internal fun ComposeContentTestRule.awaitNotLoading(timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodes(IsIndeterminateProgress).fetchSemanticsNodes().isEmpty()
    }

private val IsIndeterminateProgress = SemanticsMatcher.expectValue(
    SemanticsProperties.ProgressBarRangeInfo,
    ProgressBarRangeInfo.Indeterminate,
)
