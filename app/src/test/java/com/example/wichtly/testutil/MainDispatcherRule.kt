package com.example.wichtly.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit TestRule for controlling the main dispatcher (Dispatchers.Main) in coroutine tests.
 *
 * Why this rule?
 * Unit tests have no Android main looper. Before each test, this rule sets
 * [Dispatchers.Main] to a test dispatcher ([UnconfinedTestDispatcher]) and resets it
 * after the test using [Dispatchers.resetMain].
 */

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
