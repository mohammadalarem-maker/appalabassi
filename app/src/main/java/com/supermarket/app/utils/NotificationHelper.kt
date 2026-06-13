package com.supermarket.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.supermarket.app.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_SALES    = "sales_channel"
    private const val CHANNEL_STOCK    = "stock_channel"
    private const val CHANNEL_GENERAL  = "general_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_SALES, "المبيعات", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "إشعارات عمليات البيع"
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_STOCK, "المخزون", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "تنبيهات نفاد المخزون"
                }
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_GENERAL, "عام", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "إشعارات عامة"
                }
            )
        }
    }

    fun showSaleNotification(context: Context, invoiceNumber: String, total: Double, cashier: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_SALES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🛒 عملية بيع جديدة")
            .setContentText("فاتورة $invoiceNumber • ${"%.2f".format(total)} ر.ي • $cashier")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("رقم الفاتورة: $invoiceNumber\nالإجمالي: ${"%.2f".format(total)} ر.ي\nالكاشير: $cashier"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showLowStockNotification(context: Context, productName: String, quantity: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ تنبيه مخزون منخفض")
            .setContentText("\"$productName\" تبقى $quantity فقط")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showGeneralNotification(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
