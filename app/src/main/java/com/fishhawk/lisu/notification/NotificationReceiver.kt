package com.fishhawk.lisu.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISMISS_NOTIFICATION ->
                with(NotificationManagerCompat.from(context)) {
                    cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1))
                }
            ACTION_RETRY_APP_UPDATE ->
                AppUpdateNotification.retryChannel.trySend(
                    intent.getStringExtra(EXTRA_DOWNLOAD_URL)!!
                )
        }
    }

    companion object {
        private const val NAME = "NotificationReceiver"

        private const val ACTION_DISMISS_NOTIFICATION = "$NAME.ACTION_DISMISS_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = "$NAME.NOTIFICATION_ID"
        internal fun dismissNotificationBroadcast(
            context: Context,
            notificationId: Int
        ): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_DISMISS_NOTIFICATION
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private const val ACTION_RETRY_APP_UPDATE = "$NAME.ACTION_RETRY_APP_UPDATE"
        private const val EXTRA_DOWNLOAD_URL = "$NAME.URL"
        internal fun retryAppUpdateBroadcast(
            context: Context,
            url: String
        ): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_RETRY_APP_UPDATE
                putExtra(EXTRA_DOWNLOAD_URL, url)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}