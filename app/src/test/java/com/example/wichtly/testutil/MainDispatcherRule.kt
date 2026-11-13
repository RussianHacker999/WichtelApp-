package com.example.wichtly.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit-TestRule zur Steuerung des Haupt-Dispatchers (Dispatchers.Main) in Coroutine-Tests.
 *
 * Warum diese Rule?
 * In Unit-Tests ist kein Android-MainLooper vorhanden. Diese Rule setzt vor jedem Test
 * [Dispatchers.Main] auf einen TestDispatcher ([UnconfinedTestDispatcher]) und setzt ihn
 * nach Testende mittels [Dispatchers.resetMain] zurück.
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
