package com.example.wichtly

import android.app.Application
import com.example.wichtly.di.AppContainer
import com.example.wichtly.di.DefaultAppContainer


class WichtlyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
