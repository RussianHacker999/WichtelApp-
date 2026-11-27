package com.example.wichtly.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that controls the main dispatcher (Dispatchers.Main) in coroutine tests.
 *
 * Unit tests have no Android main looper. This rule sets [Dispatchers.Main] to a test dispatcher
 * ([UnconfinedTestDispatcher]) before every test and resets it with [Dispatchers.resetMain] afterwards.
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
