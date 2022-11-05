package com.fishhawk.lisu.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import com.fishhawk.lisu.R

object Notifications {
    fun createChannels(context: Context) = with(NotificationManagerCompat.from(context)) {
        createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(AppUpdateNotification.channel, IMPORTANCE_DEFAULT) {
                    setName("App updates")
                }
            )
        )
    }
}

private inline fun buildNotificationChannelGroup(
    channelId: String,
    block: (NotificationChannelGroupCompat.Builder.() -> Unit),
): NotificationChannelGroupCompat {
    val builder = NotificationChannelGroupCompat.Builder(channelId)
    builder.block()
    return builder.build()
}

private inline fun buildNotificationChannel(
    channelId: String,
    channelImportance: Int,
    block: (NotificationChannelCompat.Builder.() -> Unit),
): NotificationChannelCompat {
    val builder = NotificationChannelCompat.Builder(channelId, channelImportance)
    builder.block()
    return builder.build()
}
