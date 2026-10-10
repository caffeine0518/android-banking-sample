package com.study.bank.e2e.support

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag

internal fun ComposeContentTestRule.awaitTag(tag: String, timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

/** HomeViewModel은 로딩 중 새로고침 인텐트를 무시하므로, 로딩이 끝난 뒤 클릭해야 클릭이 유실되지 않는다. */
internal fun ComposeContentTestRule.awaitNotLoading(timeoutMillis: Long = 10_000) =
    waitUntil(timeoutMillis) {
        onAllNodes(IsIndeterminateProgress).fetchSemanticsNodes().isEmpty()
    }

private val IsIndeterminateProgress = SemanticsMatcher.expectValue(
    SemanticsProperties.ProgressBarRangeInfo,
    ProgressBarRangeInfo.Indeterminate,
)
