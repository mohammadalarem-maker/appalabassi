package com.supermarket.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.supermarket.app.R
import com.supermarket.app.SuperMarketApplication
import com.supermarket.app.ui.MainActivity

class SMFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "إشعار"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""
        val type  = msg.data["type"] ?: "GENERAL"
        val ch    = when(type) { "SALE" -> SuperMarketApplication.CH_SALES; "LOW_STOCK" -> SuperMarketApplication.CH_STOCK; else -> SuperMarketApplication.CH_GENERAL }
        val intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, ch)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi).setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400)).build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(System.currentTimeMillis().toInt(), notif)
    }
    override fun onNewToken(token: String) {
        getSharedPreferences("sm_prefs", Context.MODE_PRIVATE).edit().putString("fcm_token", token).apply()
    }
}
