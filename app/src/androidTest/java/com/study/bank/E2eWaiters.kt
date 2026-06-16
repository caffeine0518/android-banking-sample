package com.study.bank

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText

/**
 * E2E에서 비동기 로드(네트워크/DB)가 끝나길 기다리는 폴링 헬퍼 모음.
 *
 * 여러 E2E가 공유하므로 한곳에 모은다.
 */

/** 해당 텍스트 노드가 나타날 때까지 폴링. */
internal fun ComposeContentTestRule.awaitText(text: String, timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
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
