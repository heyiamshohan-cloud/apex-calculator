package com.shohankhan.apexcalculator

import android.app.Application

class ApexApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.warmUp()
        container.purgeExpiredHistory()
        container.scheduleBackgroundCleanup()
    }
}
