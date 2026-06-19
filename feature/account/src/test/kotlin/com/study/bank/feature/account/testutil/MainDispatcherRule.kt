package com.study.bank.feature.account.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 테스트 동안 Main 디스패처를 [testDispatcher]로 교체하는 JUnit TestRule.
 *
 * [testDispatcher]를 ViewModel(및 MviStore)에 함께 주입하면 viewModelScope와 reducer 루프가 같은
 * 디스패처를 공유해 runTest에서 결정적으로 구동된다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
