package com.fishhawk.lisu.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import com.fishhawk.lisu.R


object Notifications {
    internal const val appUpdateChannel = "app_apk_update_channel"
    internal const val appUpdateId = 1

    fun createChannels(context: Context) = with(NotificationManagerCompat.from(context)) {
        createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(appUpdateChannel, IMPORTANCE_DEFAULT) {
                    setName(context.getString(R.string.channel_app_updates))
                }
            )
        )
    }
}

private inline fun buildNotificationChannelGroup(
    channelId: String,
    block: (NotificationChannelGroupCompat.Builder.() -> Unit)
): NotificationChannelGroupCompat {
    val builder = NotificationChannelGroupCompat.Builder(channelId)
    builder.block()
    return builder.build()
}

private inline fun buildNotificationChannel(
    channelId: String,
    channelImportance: Int,
    block: (NotificationChannelCompat.Builder.() -> Unit)
): NotificationChannelCompat {
    val builder = NotificationChannelCompat.Builder(channelId, channelImportance)
    builder.block()
    return builder.build()
}
