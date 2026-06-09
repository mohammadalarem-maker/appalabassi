package com.supermarket.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SuperMarketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createChannels()
    }
    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            listOf(
                Triple(CH_SALES,   "مبيعات جديدة",     NotificationManager.IMPORTANCE_HIGH),
                Triple(CH_STOCK,   "تنبيهات المخزون",  NotificationManager.IMPORTANCE_HIGH),
                Triple(CH_GENERAL, "عام",               NotificationManager.IMPORTANCE_DEFAULT)
            ).forEach { (id, name, imp) ->
                NotificationChannel(id, name, imp).also { mgr.createNotificationChannel(it) }
            }
        }
    }
    companion object {
        const val CH_SALES   = "sm_sales"
        const val CH_STOCK   = "sm_stock"
        const val CH_GENERAL = "sm_general"
    }
}
