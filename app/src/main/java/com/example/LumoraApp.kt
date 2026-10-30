package com.example

import android.app.Application
import com.example.core.AppContainer

class LumoraApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
