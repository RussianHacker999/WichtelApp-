package com.example.wichtly.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.wichtly.WichtlyApplication
import com.example.wichtly.di.AppContainer

/**
 * Der [AppContainer] der App, aus Sicht einer Route. Einziger Weg, auf dem Routes an
 * Repositories und Use Cases kommen (manuelle Dependency Injection ohne Framework).
 */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as WichtlyApplication).container
