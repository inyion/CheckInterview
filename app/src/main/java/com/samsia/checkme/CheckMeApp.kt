package com.samsia.checkme

import android.app.Application

class CheckMeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CheckMeApp
            private set
    }
}