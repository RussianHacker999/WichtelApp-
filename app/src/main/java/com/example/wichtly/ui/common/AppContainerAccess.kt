package com.example.wichtly.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.wichtly.WichtlyApplication
import com.example.wichtly.di.AppContainer

/**
 * The app's [AppContainer], as seen from a route. The only way routes reach repositories and
 * use cases (manual dependency injection without a framework).
 */
@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as WichtlyApplication).container
